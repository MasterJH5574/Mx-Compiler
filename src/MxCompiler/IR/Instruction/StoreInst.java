package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.GlobalVariable;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.PointerType;

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
            assert ((PointerType) pointer.getType()).getBaseType().equals(value.getType());
        }
    }

    public Operand getValue() {
        return value;
    }

    public Operand getPointer() {
        return pointer;
    }

    @Override
    public String toString() {
        if (pointer instanceof GlobalVariable)
            return "store " + value.getType().toString() + " " + value.toString() +
                    ", " + (new PointerType(pointer.getType())).toString() + " " + pointer.toString();
        else
            return "store " + value.getType().toString() + " " + value.toString() +
                    ", " + pointer.getType().toString() + " " + pointer.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
