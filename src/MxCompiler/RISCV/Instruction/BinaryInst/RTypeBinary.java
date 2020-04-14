package MxCompiler.RISCV.Instruction.BinaryInst;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.Operand.Register.Register;
import MxCompiler.RISCV.BasicBlock;

public class RTypeBinary extends BinaryInst {
    public enum OpName {
        add, sub, mul, div, rem, sll, sra, and, or, xor, slt
    }

    private OpName op;
    private Register rs2;

    public RTypeBinary(BasicBlock basicBlock, OpName op, Register rs2) {
        super(basicBlock);
        this.op = op;
        this.rs2 = rs2;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
