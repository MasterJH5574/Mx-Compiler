package MxCompiler.RISCV.Instruction.BinaryInst;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Register.Register;

abstract public class BinaryInst extends ASMInstruction {
    private Register rd;
    private Register rs1;

    public BinaryInst(BasicBlock basicBlock) {
        super(basicBlock);
    }
}
