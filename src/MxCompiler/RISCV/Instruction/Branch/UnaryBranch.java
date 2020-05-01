package MxCompiler.RISCV.Instruction.Branch;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

public class UnaryBranch extends Branch {
    public enum OpName {
        beqz, bnez, bltz, bgez, blez, bgtz
    }

    private OpName op;

    public UnaryBranch(BasicBlock basicBlock, OpName op, VirtualRegister rs1, BasicBlock thenBlock) {
        super(basicBlock, rs1, thenBlock);
        this.op = op;
    }

    @Override
    public String emitCode() {
        return "\t" + op.name() + "\t" + getRs1().emitCode() + ", " + getThenBlock().emitCode();
    }

    @Override
    public String toString() {
        return op + " " + getRs1() + ", " + getThenBlock();
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
