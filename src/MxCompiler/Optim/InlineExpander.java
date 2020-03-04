package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.BranchInst;
import MxCompiler.IR.Instruction.CallInst;
import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Instruction.ReturnInst;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.VoidType;
import MxCompiler.Utilities.Pair;

import java.util.*;

public class InlineExpander extends Pass {
    private final int instructionLimit = 120;

    private Map<Function, Integer> instructionCnt;
    private Map<Function, Set<Function>> recursiveCalleeMap;

    public InlineExpander(Module module) {
        super(module);
    }

    @Override
    public boolean run() {
        instructionCnt = new HashMap<>();
        recursiveCalleeMap = new HashMap<>();
        for (Function function : module.getFunctionMap().values())
            recursiveCalleeMap.put(function, new HashSet<>());

        for (Function function : module.getFunctionMap().values())
            countInstructionsAndCalls(function);
        for (Function function : module.getFunctionMap().values())
            computeRecursiveCallees(function);

        changed = false;
        changed = nonRecursiveInline();
        changed |= recursiveInline();
        return false;
    }

    private void countInstructionsAndCalls(Function function) {
        int instructionCnt = 0;
        for (BasicBlock block : function.getBlocks()) {
            IRInstruction ptr = block.getInstHead();
            while (ptr != null) {
                instructionCnt++;
                if (ptr instanceof CallInst) {
                    Function callee = ((CallInst) ptr).getFunction();
                    if (!module.getExternalFunctionMap().containsValue(callee))
                        recursiveCalleeMap.get(function).add(callee);
                }
                ptr = ptr.getInstNext();
            }
        }
        this.instructionCnt.put(function, instructionCnt);
    }

    private void computeRecursiveCallees(Function function) {
        Queue<Function> queue = new LinkedList<>();
        Set<Function> callees = recursiveCalleeMap.get(function);
        for (Function callee : callees)
            queue.offer(callee);

        while (!queue.isEmpty()) {
            Function func = queue.poll();
            for (Function callee : recursiveCalleeMap.get(func)) {
                if (!callees.contains(callee)) {
                    callees.add(callee);
                    queue.offer(callee);
                }
            }
        }
    }

    private boolean canBeNonRecursiveInlined(Function callee, Function caller) {
        if (caller.isNotFunctional() || callee.isNotFunctional())
            return false;
        return instructionCnt.get(callee) < instructionLimit
                && callee != caller
                && !recursiveCalleeMap.get(callee).contains(callee);
    }

    private boolean canBeRecursiveInlined(Function callee, Function caller) {
        if (caller.isNotFunctional() || callee.isNotFunctional())
            return false;
        return instructionCnt.get(callee) < instructionLimit
                && callee == caller;
    }

    private Pair<ArrayList<BasicBlock>, ReturnInst> cloneCallee(Function caller,
                                                             Function callee,
                                                             ArrayList<Operand> actualParameters) {
        Map<BasicBlock, BasicBlock> blockMap = new HashMap<>();
        Map<Operand, Operand> operandMap = new HashMap<>();

        for (int i = 0; i < actualParameters.size(); i++)
            operandMap.put(callee.getParameters().get(i), actualParameters.get(i));

        ArrayList<BasicBlock> clonedBlocks = new ArrayList<>();
        for (BasicBlock block : callee.getBlocks()) {
            BasicBlock clonedBlock = (BasicBlock) block.clone();
            clonedBlock.setFunction(caller);
            clonedBlocks.add(clonedBlock);

            blockMap.put(block, clonedBlock);
            caller.getSymbolTable().put(clonedBlock.getNameWithoutDot(), clonedBlock);

            IRInstruction ptr = block.getInstHead();
            IRInstruction clonedPtr = clonedBlock.getInstHead();
            while (ptr != null && clonedPtr != null) {
                if (ptr.hasResult()) {
                    assert clonedPtr.hasResult();
                    Register result = ptr.getResult();
                    Register clonedResult = clonedPtr.getResult();
                    assert (result == null && clonedResult == null) || (result != null && clonedResult != null);
                    if (result != null) {
                        operandMap.put(result, clonedResult);
                        caller.getSymbolTable().put(clonedResult.getNameWithoutDot(), clonedResult);
                    }
                }
                ptr = ptr.getInstNext();
                clonedPtr = clonedPtr.getInstNext();
            }
            assert ptr == null && clonedPtr == null;
        }

        for (int i = 0; i < clonedBlocks.size(); i++) {
            BasicBlock clonedBlock = clonedBlocks.get(i);
            if (i != 0)
                clonedBlock.setPrev(clonedBlocks.get(i - 1));
            if (i != clonedBlocks.size() - 1)
                clonedBlock.setNext(clonedBlocks.get(i + 1));

            Set<BasicBlock> predecessors = new LinkedHashSet<>();
            Set<BasicBlock> successors = new LinkedHashSet<>();
            for (BasicBlock predecessor : clonedBlock.getPredecessors()) {
                assert blockMap.containsKey(predecessor);
                predecessors.add(blockMap.get(predecessor));
            }
            for (BasicBlock successor : clonedBlock.getSuccessors()) {
                assert blockMap.containsKey(successor);
                successors.add(blockMap.get(successor));
            }
            clonedBlock.setPredecessors(predecessors);
            clonedBlock.setSuccessors(successors);


            IRInstruction clonedPtr = clonedBlock.getInstHead();
            while (clonedPtr != null) {
                clonedPtr.clonedUseReplace(blockMap, operandMap);
                clonedPtr = clonedPtr.getInstNext();
            }
        }

        ReturnInst returnInst = null;
        for (BasicBlock clonedBlock : clonedBlocks) {
            if (clonedBlock.getInstTail() instanceof ReturnInst) {
                assert returnInst == null;
                returnInst = (ReturnInst) clonedBlock.getInstTail();
            }
        }
        assert returnInst != null;
        return new Pair<>(clonedBlocks, returnInst);
    }

    private IRInstruction inlineFunction(CallInst callInst) {
        Function caller = callInst.getBasicBlock().getFunction();
        Function callee = callInst.getFunction();
        Pair<ArrayList<BasicBlock>, ReturnInst> cloneResult = cloneCallee(caller, callee, callInst.getParameters());
        ArrayList<BasicBlock> clonedBlocks = cloneResult.getFirst();
        ReturnInst returnInst = cloneResult.getSecond();

        BasicBlock inlineDivergedBlock = callInst.getBasicBlock();
        BasicBlock inlineMergedBlock = inlineDivergedBlock.split(callInst);

        int blocksCnt = clonedBlocks.size();
        inlineDivergedBlock.setNext(clonedBlocks.get(0));
        clonedBlocks.get(0).setPrev(inlineDivergedBlock);
        inlineMergedBlock.setPrev(clonedBlocks.get(blocksCnt - 1));
        clonedBlocks.get(blocksCnt - 1).setNext(inlineMergedBlock);

        if (!(callee.getFunctionType().getReturnType() instanceof VoidType)) {
            assert !callInst.isVoidCall();
            assert returnInst.getReturnValue() != null;
            callInst.getResult().replaceUse(returnInst.getReturnValue());
        }

        returnInst.removeFromBlock();
        callInst.removeFromBlock();
        inlineDivergedBlock.addInstruction
                (new BranchInst(inlineDivergedBlock, null, clonedBlocks.get(0), null));
        clonedBlocks.get(blocksCnt - 1).addInstruction
                (new BranchInst(clonedBlocks.get(blocksCnt - 1), null, inlineMergedBlock, null));

        return inlineMergedBlock.getInstHead();
    }

    private boolean nonRecursiveInline() {
        boolean changed = false;
        while (true) {
            boolean loopChanged = false;
            for (Function function : module.getFunctionMap().values()) {
                for (BasicBlock block : function.getBlocks()) {
                    IRInstruction ptr = block.getInstHead();
                    while (ptr != null) {
                        IRInstruction next = ptr.getInstNext();
                        if (ptr instanceof CallInst) {
                            Function callee = ((CallInst) ptr).getFunction();
                            if (module.getFunctionMap().containsValue(callee)
                                    && canBeNonRecursiveInlined(callee, function)) {
                                next = inlineFunction(((CallInst) ptr));
                                instructionCnt.replace(function,
                                        instructionCnt.get(function) + instructionCnt.get(callee) - 2);
                                loopChanged = true;
                            }
                        }
                        ptr = next;
                    }
                }
            }
            if (loopChanged)
                changed = true;
            else
                break;
        }
        return changed;
    }

    private boolean recursiveInline() {
        boolean changed = false;
        final int inlineDepth = 3;
        for (int i = 0; i < inlineDepth; i++) {
            for (Function function : module.getFunctionMap().values()) {
                for (BasicBlock block : function.getBlocks()) {
                    IRInstruction ptr = block.getInstHead();
                    while (ptr != null) {
                        IRInstruction next = ptr.getInstNext();
                        if (ptr instanceof CallInst) {
                            Function callee = ((CallInst) ptr).getFunction();
                            if (module.getFunctionMap().containsValue(callee)
                                    && canBeRecursiveInlined(callee, function)) {
                                next = inlineFunction(((CallInst) ptr));
                                instructionCnt.replace(function,
                                        instructionCnt.get(function) + instructionCnt.get(callee) - 2);
                                changed = true;
                            }
                        }
                        ptr = next;
                    }
                }
            }
        }
        return changed;
    }
}
