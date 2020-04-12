package MxCompiler.RISCV.Instruction.BinaryInst;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Immediate;

public class ITypeBinary extends BinaryInst {
    public enum OpName {
        addi, slli, srai, andi, ori, xori, slti
    }

    private OpName op;
    private Immediate immediate;

    public ITypeBinary(BasicBlock basicBlock, OpName op, Immediate immediate) {
        super(basicBlock);
        this.op = op;
        this.immediate = immediate;
    }
}
