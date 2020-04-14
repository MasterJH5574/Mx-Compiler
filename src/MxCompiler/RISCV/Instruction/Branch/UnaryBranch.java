package MxCompiler.RISCV.Instruction.Branch;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Immediate;
import MxCompiler.RISCV.Operand.Register.Register;

public class UnaryBranch extends Branch {
    public enum OpName {
        beqz, bnez, bltz, bgez, blez, bgtz
    }

    private OpName op;

    public UnaryBranch(BasicBlock basicBlock, Register rs1, Immediate offset, OpName op) {
        super(basicBlock, rs1, offset);
        this.op = op;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
