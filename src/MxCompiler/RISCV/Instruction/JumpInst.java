package MxCompiler.RISCV.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.RISCV.ASMVisitor;

public class JumpInst extends ASMInstruction {
    private BasicBlock dest;

    public JumpInst(MxCompiler.RISCV.BasicBlock basicBlock, BasicBlock dest) {
        super(basicBlock);
        this.dest = dest;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
