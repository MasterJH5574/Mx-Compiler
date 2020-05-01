package MxCompiler.RISCV.Instruction.BinaryInst;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Immediate.Immediate;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

public class ITypeBinary extends BinaryInst {
    public enum OpName {
        addi, slli, srai, andi, ori, xori, slti
    }

    private OpName op;
    private Immediate immediate;

    public ITypeBinary(BasicBlock basicBlock, OpName op,
                       VirtualRegister rs1, Immediate immediate, VirtualRegister rd) {
        super(basicBlock, rd, rs1);
        this.op = op;
        this.immediate = immediate;
    }

    @Override
    public String emitCode() {
        return "\t" + op.name() + "\t"
                + getRd().emitCode() + ", " + getRs1().emitCode() + ", " + immediate.emitCode();
    }

    @Override
    public String toString() {
        return op + " " + getRd() + ", " + getRs1() + ", " + immediate;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
