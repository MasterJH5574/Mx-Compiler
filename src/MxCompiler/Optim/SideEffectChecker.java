package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.VoidType;

import java.util.*;

public class SideEffectChecker extends Pass {
    public enum Scope {
        undefined, local, outer
    }

    private Set<Function> sideEffect;
    private Map<Operand, Scope> scopeMap;
    private Map<Function, Scope> returnValueScope;
    private Boolean ignoreIO;
    private Boolean ignoreLoad;

    public SideEffectChecker(Module module) {
        super(module);
    }

    public void setIgnoreIO(boolean ignoreIO) {
        this.ignoreIO = ignoreIO;
    }

    public void setIgnoreLoad(boolean ignoreLoad) {
        this.ignoreLoad = ignoreLoad;
    }

    public boolean hasSideEffect(Function function) {
        return sideEffect.contains(function);
    }

    public boolean isOuterScope(Operand operand) {
        if (operand instanceof ConstNull)
            return false;
        return scopeMap.get(operand) == Scope.outer;
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional())
                return false;
        }

        assert ignoreIO != null;
        assert ignoreLoad != null;
        computeScope();
        checkSideEffect();
        ignoreIO = null;
        ignoreLoad = null;
        return false;
    }

    static public Scope getOperandScope(Operand operand) {
        assert operand instanceof Parameter || operand instanceof Register;
        if (operand.getType() instanceof PointerType)
            return Scope.outer;
        else
            return Scope.local;
    }

    private void computeScope() {
        scopeMap = new HashMap<>();
        returnValueScope = new HashMap<>();
        Queue<Function> queue = new LinkedList<>();
        Set<Function> inQueue = new HashSet<>();

        for (GlobalVariable globalVariable : module.getGlobalVariableMap().values())
            scopeMap.put(globalVariable, Scope.outer);
        for (Function function : module.getFunctionMap().values()) {
            for (Parameter parameter : function.getParameters())
                scopeMap.put(parameter, getOperandScope(parameter));
            for (BasicBlock block : function.getBlocks()) {
                IRInstruction ptr = block.getInstHead();
                while (ptr != null) {
                    if (ptr.hasResult()) {
                        Register result = ptr.getResult();
                        if (getOperandScope(result) == Scope.local)
                            scopeMap.put(result, Scope.local);
                        else
                            scopeMap.put(result, Scope.undefined);
                    }
                    ptr = ptr.getInstNext();
                }
            }

            if (function.getFunctionType().getReturnType() instanceof PointerType)
                returnValueScope.put(function, Scope.outer);
            else
                returnValueScope.put(function, Scope.local);
            queue.offer(function);
            inQueue.add(function);
        }
        for (Function function : module.getExternalFunctionMap().values())
            returnValueScope.put(function, Scope.local);

        while (!queue.isEmpty()) {
            Function function = queue.poll();
            inQueue.remove(function);
            computeScopeInFunction(function);

            boolean local = false;
            if (function.getFunctionType().getReturnType() instanceof VoidType)
                local = true;
            else {
                ReturnInst returnInst = ((ReturnInst) function.getExitBlock().getInstTail());
                if (scopeMap.get(returnInst.getReturnValue()) == Scope.local)
                    local = true;
            }

            if (local && returnValueScope.get(function) != Scope.local) {
                returnValueScope.replace(function, Scope.local);
                for (IRInstruction callInst : function.getUse().keySet()) {
                    assert callInst instanceof CallInst;
                    Function caller = callInst.getBasicBlock().getFunction();
                    if (!inQueue.contains(caller)) {
                        queue.offer(caller);
                        inQueue.add(caller);
                    }
                }
            }
        }

        for (Function function : module.getFunctionMap().values()) {
            for (BasicBlock block : function.getBlocks()) {
                IRInstruction ptr = block.getInstHead();
                while (ptr != null) {
                    assert !ptr.hasResult() || scopeMap.get(ptr.getResult()) != Scope.undefined;
                    ptr = ptr.getInstNext();
                }
            }
        }
    }

    private void computeScopeInFunction(Function function) {
        Queue<BasicBlock> queue = new LinkedList<>();
        Set<BasicBlock> visit = new HashSet<>();

        queue.offer(function.getEntranceBlock());
        visit.add(function.getEntranceBlock());
        while (!queue.isEmpty()) {
            BasicBlock block = queue.poll();
            boolean changed = false;

            IRInstruction ptr = block.getInstHead();
            while (ptr != null) {
                changed |= ptr.updateResultScope(scopeMap, returnValueScope);
                ptr = ptr.getInstNext();
            }

            if (block.getInstTail() instanceof BranchInst) {
                BranchInst branchInst = ((BranchInst) block.getInstTail());
                if (!visit.contains(branchInst.getThenBlock())) {
                    queue.offer(branchInst.getThenBlock());
                    visit.add(branchInst.getThenBlock());
                } else if (changed)
                    queue.offer(branchInst.getThenBlock());

                if (branchInst.isConditional()) {
                    if (!visit.contains(branchInst.getElseBlock())) {
                        queue.offer(branchInst.getElseBlock());
                        visit.add(branchInst.getElseBlock());
                    } else if (changed)
                        queue.offer(branchInst.getElseBlock());
                }
            }
        }
    }

    private void checkSideEffect() {
        sideEffect = new HashSet<>();
        Queue<Function> queue = new LinkedList<>();

        if (!ignoreIO) {
            for (Function externalFunction : module.getExternalFunctionMap().values()) {
                if (externalFunction.hasSideEffect()) {
                    sideEffect.add(externalFunction);
                    queue.offer(externalFunction);
                }
            }
        }
        for (Function function : module.getFunctionMap().values()) {
            boolean hasSideEffect = false;
            for (BasicBlock block : function.getBlocks()) {
                IRInstruction ptr = block.getInstHead();
                while (ptr != null) {
                    if (ptr instanceof StoreInst && scopeMap.get(((StoreInst) ptr).getPointer()) == Scope.outer) {
                        hasSideEffect = true;
                        break;
                    }
                    if (!ignoreLoad && ptr instanceof LoadInst
                            && scopeMap.get(((LoadInst) ptr).getPointer()) == Scope.outer) {
                        hasSideEffect = true;
                        break;
                    }
                    ptr = ptr.getInstNext();
                }
                if (hasSideEffect) {
                    sideEffect.add(function);
                    queue.offer(function);
                    break;
                }
            }
        }

        while (!queue.isEmpty()) {
            Function function = queue.poll();
            for (IRInstruction callInst : function.getUse().keySet()) {
                assert callInst instanceof CallInst;
                Function caller = callInst.getBasicBlock().getFunction();
                if (!sideEffect.contains(caller)) {
                    sideEffect.add(caller);
                    queue.offer(caller);
                }
            }
        }
    }
}
