package MxCompiler.RISCV.Operand;

public class GlobalVariable extends ASMOperand {
    private String name;

    public GlobalVariable(String name) {
        this.name = name;
    }
}
