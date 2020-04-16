package MxCompiler.RISCV.Operand.Address;

import MxCompiler.RISCV.Operand.Immediate.Immediate;
import MxCompiler.RISCV.Operand.Register.Register;

public class BaseOffsetAddr extends Address {
    private Register base;
    private Immediate offset;

    public BaseOffsetAddr(Register base, Immediate offset) {
        this.base = base;
        this.offset = offset;
    }
}
