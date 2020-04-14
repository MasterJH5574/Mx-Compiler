package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.Operand.Register.Register;
import MxCompiler.RISCV.BasicBlock;

public class UnaryInst extends ASMInstruction {
    public enum OpName {
        seqz, snez, sltz, sgtz
    }

    private OpName op;
    private Register rd;
    private Register rs;

    public UnaryInst(BasicBlock basicBlock, OpName op, Register rd, Register rs) {
        super(basicBlock);
        this.op = op;
        this.rd = rd;
        this.rs = rs;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
