package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IntegerType;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.StructureType;
import MxCompiler.Optim.Andersen;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.LoopOptim.LICM;
import MxCompiler.Optim.LoopOptim.LoopAnalysis;
import MxCompiler.Optim.SCCP;
import MxCompiler.Optim.SideEffectChecker;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class LoadInst extends IRInstruction {
    private IRType type;
    private Operand pointer;
    private Register result;

    public LoadInst(BasicBlock basicBlock, IRType type, Operand pointer, Register result) {
        super(basicBlock);
        this.type = type;
        this.pointer = pointer;
        this.result = result;

        if (pointer instanceof GlobalVariable)
            assert pointer.getType().equals(type);
        else {
            assert pointer.getType() instanceof PointerType;
            assert ((PointerType) pointer.getType()).getBaseType().equals(type);
        }
        assert result.getType().equals(type);
        assert type instanceof IntegerType || type instanceof PointerType;
    }

    @Override
    public void successfullyAdd() {
        result.setDef(this);
        pointer.addUse(this);
    }

    public IRType getType() {
        return type;
    }

    public Operand getPointer() {
        return pointer;
    }

    @Override
    public Register getResult() {
        return result;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (pointer == oldUse) {
            pointer.removeUse(this);
            pointer = (Operand) newUse;
            pointer.addUse(this);
        }
    }

    @Override
    public void removeFromBlock() {
        pointer.removeUse(this);
        super.removeFromBlock();
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        pointer.markBaseAsLive(live, queue);
    }

    @Override
    public boolean replaceResultWithConstant(SCCP sccp) {
        SCCP.Status status = sccp.getStatus(result);
        if (status.getOperandStatus() == SCCP.Status.OperandStatus.constant) {
            result.replaceUse(status.getOperand());
            this.removeFromBlock();
            return true;
        } else
            return false;
    }

    @Override
    public CSE.Expression convertToExpression() {
        String instructionName = "load";
        ArrayList<String> operands = new ArrayList<>();
        operands.add(pointer.toString());
        return new CSE.Expression(instructionName, operands);
    }

    @Override
    public void clonedUseReplace(Map<BasicBlock, BasicBlock> blockMap, Map<Operand, Operand> operandMap) {
        if (pointer instanceof Parameter || pointer instanceof Register) {
            assert operandMap.containsKey(pointer);
            pointer = operandMap.get(pointer);
        }
        pointer.addUse(this);
    }

    @Override
    public void addConstraintsForAndersen(Map<Operand, Andersen.Node> nodeMap, Set<Andersen.Node> nodes) {
        // result = *pointer -> *pointer <= result
        if (!(result.getType() instanceof PointerType))
            return;
        if (!(pointer instanceof ConstNull)) {
            assert nodeMap.containsKey(pointer);
            assert nodeMap.containsKey(result);
            nodeMap.get(pointer).getDereferenceLhs().add(nodeMap.get(result));
        }
    }

    @Override
    public boolean updateResultScope(Map<Operand, SideEffectChecker.Scope> scopeMap,
                                     Map<Function, SideEffectChecker.Scope> returnValueScope) {
        if (SideEffectChecker.getOperandScope(result) == SideEffectChecker.Scope.local
                || pointer instanceof ConstNull) {
            if (scopeMap.get(result) != SideEffectChecker.Scope.local) {
                scopeMap.replace(result, SideEffectChecker.Scope.local);
                return true;
            } else
                return false;
        } else {
            SideEffectChecker.Scope scope = scopeMap.get(pointer);
            assert scope != SideEffectChecker.Scope.undefined;
            if (scopeMap.get(result) != scope) {
                scopeMap.replace(result, scope);
                return true;
            } else
                return false;
        }
    }

    @Override
    public boolean checkLoopInvariant(LoopAnalysis.LoopNode loop, LICM licm) {
        if (licm.isLoopInvariant(result, loop))
            return false;

        if (!licm.isLoopInvariant(pointer, loop))
            return false;
        if (licm.getSideEffectCall().contains(loop))
            return false;
        Set<StoreInst> stores = licm.getStoreMap().get(loop);
        for (StoreInst storeInst : stores) {
            if (licm.mayAlias(this.pointer, storeInst.getPointer()))
                return false;
        }

        licm.markLoopInvariant(result);
        return true;
    }

    @Override
    public boolean canBeHoisted(LoopAnalysis.LoopNode loop) {
        return loop.defOutOfLoop(pointer);
    }

    @Override
    public boolean combineInst(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        return false;
    }

    @Override
    public String toString() {
        if (pointer instanceof GlobalVariable)
            return result.toString() + " = load " + type.toString() +
                    ", " + (new PointerType(pointer.getType())).toString() + " " + pointer.toString();
        else
            return result.toString() + " = load "
                    + type.toString() + ", " + pointer.getType().toString() + " " + pointer.toString();
    }

    @Override
    public Object clone() {
        LoadInst loadInst = (LoadInst) super.clone();
        loadInst.type = this.type;
        loadInst.pointer = this.pointer;
        loadInst.result = (Register) this.result.clone();

        loadInst.result.setDef(loadInst);
        return loadInst;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
