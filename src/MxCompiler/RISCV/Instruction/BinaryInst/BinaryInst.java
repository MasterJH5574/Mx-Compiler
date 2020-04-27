package MxCompiler.RISCV.Instruction.BinaryInst;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Register.Register;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

abstract public class BinaryInst extends ASMInstruction {
    private Register rd;
    private Register rs1;

    public BinaryInst(BasicBlock basicBlock, Register rd, Register rs1) {
        super(basicBlock);
        this.rd = rd;
        this.rs1 = rs1;

        this.rd.addDef(this);
        this.rs1.addUse(this);
    }

    @Override
    public void addToUEVarAndVarKill(Set<Register> UEVar, Set<Register> varKill) {
        if (!varKill.contains(rs1))
            UEVar.add(rs1);
        varKill.add(rd);
    }
}
