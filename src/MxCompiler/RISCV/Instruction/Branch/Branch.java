package MxCompiler.RISCV.Instruction.Branch;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Register.Register;

import java.util.Set;

abstract public class Branch extends ASMInstruction {
    private Register rs1;
    private BasicBlock thenBlock;

    public Branch(BasicBlock basicBlock, Register rs1, BasicBlock thenBlock) {
        super(basicBlock);
        this.rs1 = rs1;
        this.thenBlock = thenBlock;

        this.rs1.addUse(this);
        this.addUse(this.rs1);
    }

    @Override
    public void addToUEVarAndVarKill(Set<Register> UEVar, Set<Register> varKill) {
        if (!varKill.contains(rs1))
            UEVar.add(rs1);
    }
}
