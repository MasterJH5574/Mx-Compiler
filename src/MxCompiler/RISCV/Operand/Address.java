package MxCompiler.RISCV.Operand;

import MxCompiler.RISCV.Operand.Register.Register;

public class Address {
    private Register base;
    private int offset;

    public Address(Register base, int offset) {
        this.base = base;
        this.offset = offset;
    }
}
