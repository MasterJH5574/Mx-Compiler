package MxCompiler.RISCV.Operand;

public class Immediate extends ASMOperand {
    public enum ImmType {
        integer, relocation
    }

    private ImmType type;
    private RelocationExpansion relocation;
    private int immediate;

    public Immediate(ImmType type, RelocationExpansion relocation, int immediate) {
        this.type = type;
        this.relocation = relocation;
        this.immediate = immediate;
    }
}
