package MxCompiler.RISCV.Operand;

public class Immediate extends ASMOperand {
    private int immediate;

    public Immediate(int immediate) {
        this.immediate = immediate;
    }
}
