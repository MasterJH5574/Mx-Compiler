package MxCompiler.RISCV.Operand.Address;

import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Immediate.Immediate;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

public class BaseOffsetAddr extends Address {
    private VirtualRegister base;
    private Immediate offset;

    public BaseOffsetAddr(VirtualRegister base, Immediate offset) {
        this.base = base;
        this.offset = offset;
    }

    @Override
    public void addToUEVarAndVarKill(Set<VirtualRegister> UEVar, Set<VirtualRegister> varKill) {
        if (!varKill.contains(base))
            UEVar.add(base);
    }

    @Override
    public void addBaseUse(ASMInstruction use) {
        use.addUse(base);
        base.addUse(use);
    }

    @Override
    public void replaceUse(VirtualRegister oldVR, VirtualRegister newVR) {
        if (base == oldVR)
            base = newVR;
        super.replaceUse(oldVR, newVR);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BaseOffsetAddr))
            return false;
        return base == ((BaseOffsetAddr) obj).base && offset.equals(((BaseOffsetAddr) obj).offset);
    }
}
