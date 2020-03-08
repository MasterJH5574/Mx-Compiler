package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.CallInst;
import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Instruction.ReturnInst;
import MxCompiler.IR.Instruction.StoreInst;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.GlobalVariable;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.VoidType;

import java.util.*;

public class SideEffectChecker extends Pass {
    public enum Scope {
        local, outer
    }

    private Set<Function> sideEffect;
    private Map<Operand, Scope> scopeMap;
    private Boolean ignoreIO;

    public SideEffectChecker(Module module) {
        super(module);
    }

    public void setIgnoreIO(boolean ignoreIO) {
        this.ignoreIO = ignoreIO;
    }

    public boolean hasSideEffect(Function function) {
        return sideEffect.contains(function);
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional())
                return false;
        }

        assert ignoreIO != null;
        computeScope();
        checkSideEffect();
        ignoreIO = null;
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
        Map<Function, Scope> returnValueScope = new HashMap<>();
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
                        scopeMap.put(result, Scope.outer);
                    }
                    ptr = ptr.getInstNext();
                }
            }

            returnValueScope.put(function, Scope.outer);
            queue.offer(function);
            inQueue.add(function);
        }
        for (Function function : module.getExternalFunctionMap().values())
            returnValueScope.put(function, Scope.local);

        while (!queue.isEmpty()) {
            Function function = queue.poll();
            inQueue.remove(function);
            for (BasicBlock block : function.getDFSOrder()) {
                IRInstruction ptr = block.getInstHead();
                while (ptr != null) {
                    ptr.updateResultScope(scopeMap, returnValueScope);
                    ptr = ptr.getInstNext();
                }
            }

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
