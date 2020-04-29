package MxCompiler.RISCV.Operand.Address;

import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Register.Register;

import java.util.Set;

abstract public class Address {
    public void addToUEVarAndVarKill(Set<Register> UEVar, Set<Register> varKill) {

    }

    public void addBaseUse(ASMInstruction use) {

    }

    @Override
    abstract public boolean equals(Object obj);
}
