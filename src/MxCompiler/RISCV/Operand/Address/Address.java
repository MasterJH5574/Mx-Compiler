package MxCompiler.RISCV.Operand.Address;

import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

abstract public class Address {
    public void addToUEVarAndVarKill(Set<VirtualRegister> UEVar, Set<VirtualRegister> varKill) {

    }

    public void addBaseUse(ASMInstruction use) {

    }

    public void replaceUse(VirtualRegister oldVR, VirtualRegister newVR) {

    }

    abstract public String emitCode();

    @Override
    abstract public String toString();

    @Override
    abstract public boolean equals(Object obj);
}
