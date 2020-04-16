package MxCompiler.RISCV.Instruction.Branch;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Register.Register;

abstract public class Branch extends ASMInstruction {
    private Register rs1;
    private BasicBlock thenBlock;

    public Branch(BasicBlock basicBlock, Register rs1, BasicBlock thenBlock) {
        super(basicBlock);
        this.rs1 = rs1;
        this.thenBlock = thenBlock;
    }
}
