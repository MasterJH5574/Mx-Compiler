package MxCompiler.RISCV.Instruction.Branch;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Immediate;
import MxCompiler.RISCV.Operand.Register.Register;

abstract public class Branch extends ASMInstruction {
    private Register rs1;
    private Immediate offset;

    public Branch(BasicBlock basicBlock, Register rs1, Immediate offset) {
        super(basicBlock);
        this.rs1 = rs1;
        this.offset = offset;
    }
}
