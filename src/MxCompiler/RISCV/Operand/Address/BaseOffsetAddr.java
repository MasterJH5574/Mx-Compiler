package MxCompiler.RISCV.Operand.Address;

import MxCompiler.RISCV.Operand.Register.Register;

public class BaseOffsetAddr extends Address {
    private Register base;
    private int offset;

    public BaseOffsetAddr(Register base, int offset) {
        this.base = base;
        this.offset = offset;
    }
}
