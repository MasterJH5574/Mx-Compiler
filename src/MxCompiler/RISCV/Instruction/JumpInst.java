package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.ASMVisitor;

public class JumpInst extends ASMInstruction {
    private BasicBlock dest;

    public JumpInst(MxCompiler.RISCV.BasicBlock basicBlock, BasicBlock dest) {
        super(basicBlock);
        this.dest = dest;
    }

    @Override
    public String emitCode() {
        return "\tj\t" + dest.emitCode();
    }

    @Override
    public String toString() {
        return "j " + dest;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
