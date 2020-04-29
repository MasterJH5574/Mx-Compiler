package MxCompiler.RISCV.Instruction.BinaryInst;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

abstract public class BinaryInst extends ASMInstruction {
    private VirtualRegister rd;
    private VirtualRegister rs1;

    public BinaryInst(BasicBlock basicBlock, VirtualRegister rd, VirtualRegister rs1) {
        super(basicBlock);
        this.rd = rd;
        this.rs1 = rs1;

        this.rd.addDef(this);
        this.rs1.addUse(this);
        this.addDef(this.rd);
        this.addUse(this.rs1);
    }

    @Override
    public void addToUEVarAndVarKill(Set<VirtualRegister> UEVar, Set<VirtualRegister> varKill) {
        if (!varKill.contains(rs1))
            UEVar.add(rs1);
        varKill.add(rd);
    }

    @Override
    public void replaceDef(VirtualRegister oldVR, VirtualRegister newVR) {
        assert rd == oldVR;
        rd = newVR;
        super.replaceDef(oldVR, newVR);
    }

    @Override
    public void replaceUse(VirtualRegister oldVR, VirtualRegister newVR) {
        if (rs1 == oldVR)
            rs1 = newVR;
        super.replaceUse(oldVR, newVR);
    }
}
