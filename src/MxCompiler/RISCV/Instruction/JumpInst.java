package MxCompiler.RISCV.Instruction;

import MxCompiler.IR.BasicBlock;

public class JumpInst extends ASMInstruction {
    private BasicBlock dest;

    public JumpInst(MxCompiler.RISCV.BasicBlock basicBlock, BasicBlock dest) {
        super(basicBlock);
        this.dest = dest;
    }
}
