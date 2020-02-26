package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.GlobalVariable;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.Optim.SCCP;

import java.util.Queue;
import java.util.Set;

public class StoreInst extends IRInstruction {
    private Operand value;
    private Operand pointer;

    public StoreInst(BasicBlock basicBlock, Operand value, Operand pointer) {
        super(basicBlock);
        this.value = value;
        this.pointer = pointer;

        if (pointer instanceof GlobalVariable)
            assert pointer.getType().equals(value.getType());
        else {
            assert pointer.getType() instanceof PointerType;
            assert ((PointerType) pointer.getType()).getBaseType().equals(value.getType())
                    || value instanceof ConstNull;
        }
    }

    @Override
    public void successfullyAdd() {
        value.addUse(this);
        pointer.addUse(this);
    }

    public Operand getValue() {
        return value;
    }

    public Operand getPointer() {
        return pointer;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (value == oldUse) {
            value = (Operand) newUse;
            value.addUse(this);
        }
        if (pointer == oldUse) {
            pointer = (Operand) newUse;
            pointer.addUse(this);
        }
    }

    @Override
    public void removeFromBlock() {
        value.removeUse(this);
        pointer.removeUse(this);
        super.removeFromBlock();
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        value.markBaseAsLive(live, queue);
        pointer.markBaseAsLive(live, queue);
    }

    @Override
    public boolean replaceResultWithConstant(SCCP sccp) {
        // Do nothing.
        return false;
    }

    @Override
    public String toString() {
        if (pointer instanceof GlobalVariable)
                return "store " + pointer.getType().toString() + " " + value.toString() +
                        ", " + (new PointerType(pointer.getType())).toString() + " " + pointer.toString();
        else
            return "store " + ((PointerType) pointer.getType()).getBaseType().toString() + " " + value.toString() +
                    ", " + pointer.getType().toString() + " " + pointer.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
