package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Immediate;
import MxCompiler.RISCV.Operand.Register.Register;

public class LoadImmediate extends ASMInstruction {
    private Register rd;
    private Immediate immediate;

    public LoadImmediate(BasicBlock basicBlock, Register rd, Immediate immediate) {
        super(basicBlock);
        this.rd = rd;
        this.immediate = immediate;
    }
}
