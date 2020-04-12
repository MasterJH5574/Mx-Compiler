package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Register.Register;

public class MoveInst extends ASMInstruction {
    private Register rd;
    private Register rs;

    public MoveInst(BasicBlock basicBlock, Register rd, Register rs) {
        super(basicBlock);
        this.rd = rd;
        this.rs = rs;
    }
}
