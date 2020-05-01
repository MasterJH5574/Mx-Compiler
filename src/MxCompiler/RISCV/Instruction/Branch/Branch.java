package MxCompiler.RISCV.Instruction.Branch;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

abstract public class Branch extends ASMInstruction {
    private VirtualRegister rs1;
    private BasicBlock thenBlock;

    public Branch(BasicBlock basicBlock, VirtualRegister rs1, BasicBlock thenBlock) {
        super(basicBlock);
        this.rs1 = rs1;
        this.thenBlock = thenBlock;

        this.rs1.addUse(this);
        this.addUse(this.rs1);
    }

    public VirtualRegister getRs1() {
        return rs1;
    }

    public BasicBlock getThenBlock() {
        return thenBlock;
    }

    @Override
    public void addToUEVarAndVarKill(Set<VirtualRegister> UEVar, Set<VirtualRegister> varKill) {
        if (!varKill.contains(rs1))
            UEVar.add(rs1);
    }

    @Override
    public void replaceUse(VirtualRegister oldVR, VirtualRegister newVR) {
        if (rs1 == oldVR)
            rs1 = newVR;
        super.replaceUse(oldVR, newVR);
    }
}
