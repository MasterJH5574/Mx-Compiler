package MxCompiler.RISCV.Operand.Immediate;

public class IntImmediate extends Immediate {
    int value;

    public IntImmediate(long value) {
        assert value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE;
        this.value = ((int) value);
    }

    public void minusImmediate() {
        this.value = -this.value;
    }
}
