package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;

public class StoreInst extends IRInstruction {
    private Operand value;
    private Operand pointer;

    public StoreInst(BasicBlock basicBlock, Operand value, Operand pointer) {
        super(basicBlock);
        this.value = value;
        this.pointer = pointer;
    }

    public Operand getValue() {
        return value;
    }

    public Operand getPointer() {
        return pointer;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
