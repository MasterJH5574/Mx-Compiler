package MxCompiler.RISCV.Instruction.Branch;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Immediate;
import MxCompiler.RISCV.Operand.Register.Register;

public class BinaryBranch extends Branch {
    public enum OpName {
        beq, bne, blt, bge, ble, bgt
    }

    private OpName op;
    private Register rs2;

    public BinaryBranch(BasicBlock basicBlock, OpName op, Register rs1, Register rs2, Immediate offset) {
        super(basicBlock, rs1, offset);
        this.op = op;
        this.rs2 = rs2;
    }
}
