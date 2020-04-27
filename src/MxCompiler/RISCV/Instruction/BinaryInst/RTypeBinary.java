package MxCompiler.RISCV.Instruction.BinaryInst;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.Operand.Register.Register;
import MxCompiler.RISCV.BasicBlock;

import java.util.Set;

public class RTypeBinary extends BinaryInst {
    public enum OpName {
        add, sub, mul, div, rem, sll, sra, and, or, xor, slt
    }

    private OpName op;
    private Register rs2;

    public RTypeBinary(BasicBlock basicBlock, OpName op, Register rs1, Register rs2, Register rd) {
        super(basicBlock, rd, rs1);
        this.op = op;
        this.rs2 = rs2;

        this.rs2.addUse(this);
    }

    @Override
    public void addToUEVarAndVarKill(Set<Register> UEVar, Set<Register> varKill) {
        if (!varKill.contains(rs2))
            UEVar.add(rs2);
        super.addToUEVarAndVarKill(UEVar, varKill);
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
