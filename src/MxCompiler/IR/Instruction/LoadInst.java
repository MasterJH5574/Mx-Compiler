package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.Optim.Andersen;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.SCCP;

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
        // Without alias analysis, load instruction cannot be used for CSE.
        throw new RuntimeException("Convert load instruction to expression.");
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
        // result = *pointer
        if (!(result.getType() instanceof PointerType))
            return;
        if (!(pointer instanceof ConstNull)) {
            assert nodeMap.containsKey(pointer);
            assert nodeMap.containsKey(result);
            nodeMap.get(pointer).getDereferenceRhs().add(nodeMap.get(result));
        }
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
