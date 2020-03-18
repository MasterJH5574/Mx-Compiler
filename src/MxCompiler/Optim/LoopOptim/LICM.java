package MxCompiler.Optim.LoopOptim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.*;
import MxCompiler.Optim.Andersen;
import MxCompiler.Optim.Pass;
import MxCompiler.Optim.SideEffectChecker;

import java.util.*;

public class LICM extends Pass {
    private LoopAnalysis loopAnalysis;
    private SideEffectChecker sideEffectChecker;
    private SideEffectChecker sideEffectCheckerIgnoreLoad;
    private Andersen andersen;

    private Set<Register> loopInvariant;
    private Map<LoopAnalysis.LoopNode, Set<StoreInst>> storeMap;
    private Set<LoopAnalysis.LoopNode> sideEffectCall;

    public LICM(Module module, LoopAnalysis loopAnalysis, SideEffectChecker sideEffectChecker, Andersen andersen) {
        super(module);
        this.loopAnalysis = loopAnalysis;
        this.sideEffectChecker = sideEffectChecker;
        this.sideEffectCheckerIgnoreLoad = new SideEffectChecker(module);
        this.andersen = andersen;
    }

    public Map<LoopAnalysis.LoopNode, Set<StoreInst>> getStoreMap() {
        return storeMap;
    }

    public Set<LoopAnalysis.LoopNode> getSideEffectCall() {
        return sideEffectCall;
    }

    public boolean isLoopInvariant(Operand operand, LoopAnalysis.LoopNode loop) {
        if (operand instanceof Parameter || operand instanceof Constant || operand instanceof GlobalVariable)
            return true;
        assert operand instanceof Register;
        if (!loop.getLoopBlocks().contains(((Register) operand).getDef().getBasicBlock()))
            return true;
        return loopInvariant.contains(operand);
    }

    public void markLoopInvariant(Register register) {
        loopInvariant.add(register);
    }

    public boolean hasSideEffect(Function function) {
        return sideEffectChecker.hasSideEffect(function);
    }

    public boolean mayAlias(Operand op1, Operand op2) {
        return andersen.mayAlias(op1, op2);
    }

    private void hoistInstruction(IRInstruction instruction, BasicBlock preHeader) {
        BranchInst tail = ((BranchInst) preHeader.getInstTail());

        instruction.removeFromBlockWithoutRemoveUse();
        if (tail.getInstPrev() == null) {
            preHeader.setInstHead(instruction);
            instruction.setInstPrev(null);
        } else {
            tail.getInstPrev().setInstNext(instruction);
            instruction.setInstPrev(tail.getInstPrev());
        }
        instruction.setInstNext(tail);
        tail.setInstPrev(instruction);

        instruction.setBasicBlock(preHeader);
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional())
                return false;
        }

        sideEffectChecker.setIgnoreIO(false);
        sideEffectChecker.setIgnoreLoad(false);
        sideEffectChecker.run();
        sideEffectCheckerIgnoreLoad.setIgnoreIO(true);
        sideEffectCheckerIgnoreLoad.setIgnoreLoad(true);
        sideEffectCheckerIgnoreLoad.run();

        loopInvariant = new HashSet<>();
        storeMap = new HashMap<>();
        sideEffectCall = new HashSet<>();

        changed = false;
        for (Function function : module.getFunctionMap().values())
            changed |= dfsLicm(loopAnalysis.getLoopRoot().get(function));
        return changed;
    }

    private boolean dfsLicm(LoopAnalysis.LoopNode loop) {
        Set<StoreInst> stores = new HashSet<>();
        boolean changed = false;
        boolean sideEffectCall = false;
        for (LoopAnalysis.LoopNode child : loop.getChildren()) {
            changed |= dfsLicm(child);
            stores.addAll(storeMap.get(child));
            sideEffectCall |= this.sideEffectCall.contains(child);
        }

        if (loop.hasFather()) {
            for (BasicBlock block : loop.getUniqueLoopBlocks()) {
                IRInstruction ptr = block.getInstHead();
                while (ptr != null) {
                    if (ptr instanceof StoreInst)
                        stores.add(((StoreInst) ptr));
                    else if (ptr instanceof CallInst
                            && sideEffectCheckerIgnoreLoad.hasSideEffect(((CallInst) ptr).getFunction()))
                        sideEffectCall = true;
                    ptr = ptr.getInstNext();
                }
            }

            storeMap.put(loop, stores);
            if (sideEffectCall)
                this.sideEffectCall.add(loop);
            changed |= licm(loop);
        }
        return changed;
    }

    private boolean licm(LoopAnalysis.LoopNode loop) {
        boolean hasPreHeader = loop.hasPreHeader(loopAnalysis.getBlockNodeMap());
        assert hasPreHeader;

        boolean changed = false;
        Queue<IRInstruction> invariantQueue = new LinkedList<>();
        while (true) {
            boolean loopChanged = false;
            for (BasicBlock block : loop.getUniqueLoopBlocks()) {
                IRInstruction ptr = block.getInstHead();
                while (ptr != null) {
                    if (ptr.checkLoopInvariant(loop, this)) {
                        invariantQueue.offer(ptr);
                        loopChanged = true;
                    }
                    ptr = ptr.getInstNext();
                }
            }
            if (!loopChanged)
                break;
            else
                changed = true;
        }

        while (!invariantQueue.isEmpty()) {
            IRInstruction instruction = invariantQueue.poll();
            assert instruction.hasResult();
            assert loopInvariant.contains(instruction.getResult());
            if (instruction.canBeHoisted(loop))
                hoistInstruction(instruction, loop.getPreHeader());
            else
                invariantQueue.offer(instruction);
        }

        return changed;
    }
}
