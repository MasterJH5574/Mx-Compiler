package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.BasicBlock;

public class ReturnInst extends ASMInstruction {
    public ReturnInst(BasicBlock basicBlock) {
        super(basicBlock);
    }
}
