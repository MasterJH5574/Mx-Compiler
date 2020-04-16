package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.Operand.Register.Register;
import MxCompiler.RISCV.BasicBlock;

public class UnaryInst extends ASMInstruction {
    public enum OpName {
        seqz, snez, sltz, sgtz
    }

    private OpName op;
    private Register rs;
    private Register rd;

    public UnaryInst(BasicBlock basicBlock, OpName op, Register rs, Register rd) {
        super(basicBlock);
        this.op = op;
        this.rs = rs;
        this.rd = rd;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
