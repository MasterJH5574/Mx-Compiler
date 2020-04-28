package MxCompiler.RISCV.Operand.Address;

import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Immediate.Immediate;
import MxCompiler.RISCV.Operand.Register.Register;

import java.util.Set;

public class BaseOffsetAddr extends Address {
    private Register base;
    private Immediate offset;

    public BaseOffsetAddr(Register base, Immediate offset) {
        this.base = base;
        this.offset = offset;
    }

    @Override
    public void addToUEVarAndVarKill(Set<Register> UEVar, Set<Register> varKill) {
        if (!varKill.contains(base))
            UEVar.add(base);
    }

    @Override
    public void addBaseUse(ASMInstruction use) {
        use.addUse(base);
        base.addUse(use);
    }
}
