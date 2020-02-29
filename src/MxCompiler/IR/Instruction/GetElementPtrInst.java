package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.GlobalVariable;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.ArrayType;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IntegerType;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.SCCP;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Set;

public class GetElementPtrInst extends IRInstruction {
    private Operand pointer;
    private ArrayList<Operand> index;
    private Register result;

    public GetElementPtrInst(BasicBlock basicBlock, Operand pointer, ArrayList<Operand> index, Register result) {
        super(basicBlock);
        this.pointer = pointer;
        this.index = index;
        this.result = result;

        assert pointer.getType() instanceof PointerType
                || (pointer instanceof GlobalVariable && pointer.getType() instanceof ArrayType);
        if (pointer.getType() instanceof PointerType)
            assert result.getType() instanceof PointerType;
        else
            assert result.getType().equals(new PointerType(new IntegerType(IntegerType.BitWidth.int8)));
    }

    @Override
    public void successfullyAdd() {
        result.setDef(this);

        pointer.addUse(this);
        for (Operand operand : index)
            operand.addUse(this);
    }

    public Operand getPointer() {
        return pointer;
    }

    public ArrayList<Operand> getIndex() {
        return index;
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
        for (int i = 0; i < index.size(); i++) {
            if (index.get(i) == oldUse) {
                index.set(i, (Operand) newUse);
                index.get(i).addUse(this);
            }
        }
    }

    @Override
    public void removeFromBlock() {
        pointer.removeUse(this);
        for (Operand operand : index)
            operand.removeUse(this);
        super.removeFromBlock();
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        pointer.markBaseAsLive(live, queue);
        for (Operand operand : index)
            operand.markBaseAsLive(live, queue);
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
        String instructionName = "getelementptr";
        ArrayList<String> operands = new ArrayList<>();
        operands.add(pointer.toString());
        for (Operand operand : index)
            operands.add(operand.toString());
        return new CSE.Expression(instructionName, operands);
    }

    @Override
    public String toString() {
        IRType baseType;
        IRType pointerType;
        if (pointer.getType() instanceof PointerType) {
            baseType = ((PointerType) pointer.getType()).getBaseType();
            pointerType = pointer.getType();
        } else {
            baseType = pointer.getType();
            pointerType = new PointerType(baseType);
        }
        StringBuilder string = new StringBuilder();
        string.append(result.toString()).append(" = ");
        string.append("getelementptr ").append(baseType.toString()).append(", ");
        string.append(pointerType).append(" ").append(pointer.toString());
        for (Operand aIndex : index)
            string.append(", ").append(aIndex.getType().toString()).append(" ").append(aIndex.toString());
        return string.toString();
    }

    @Override
    public Object clone() {
        GetElementPtrInst getElementPtrInst = (GetElementPtrInst) super.clone();
        getElementPtrInst.pointer = this.pointer;
        getElementPtrInst.index = new ArrayList<>(index);
        getElementPtrInst.result = (Register) this.result.clone();

        getElementPtrInst.result.setDef(getElementPtrInst);
        return getElementPtrInst;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
