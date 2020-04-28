package MxCompiler.RISCV.Instruction.Branch;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Register.Register;

import java.util.Set;

public class BinaryBranch extends Branch {
    public enum OpName {
        beq, bne, blt, bge, ble, bgt
    }

    private OpName op;
    private Register rs2;

    public BinaryBranch(BasicBlock basicBlock, OpName op, Register rs1, Register rs2, BasicBlock thenBlock) {
        super(basicBlock, rs1, thenBlock);
        this.op = op;
        this.rs2 = rs2;

        this.rs2.addUse(this);
        this.addUse(this.rs2);
    }

    @Override
    public void addToUEVarAndVarKill(Set<Register> UEVar, Set<Register> varKill) {
        super.addToUEVarAndVarKill(UEVar, varKill);
        if (!varKill.contains(rs2))
            UEVar.add(rs2);
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
