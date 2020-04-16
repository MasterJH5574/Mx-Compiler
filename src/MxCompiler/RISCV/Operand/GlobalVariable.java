package MxCompiler.RISCV.Operand;

public class GlobalVariable extends ASMOperand {
    private String name;

    private boolean isString;
    private String string;

    public GlobalVariable(String name) {
        this.name = name;
        isString = false;
        string = null;
    }

    public void setString(String string) {
        isString = true;
        assert string.charAt(string.length() - 1) == '\0';
        this.string = string.substring(0, string.length() - 1);
    }
}
