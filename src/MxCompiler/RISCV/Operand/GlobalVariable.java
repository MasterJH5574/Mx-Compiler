package MxCompiler.RISCV.Operand;

public class GlobalVariable extends ASMOperand {
    private String name;

    private boolean isString;
    private String string;

    private boolean isInt;
    private int word;

    private boolean isBool;
    private int boolByte;

    public GlobalVariable(String name) {
        this.name = name;

        isString = false;
        string = null;
        isInt = false;
        word = 0;
        isBool = false;
        boolByte = 0;
    }

    public void setString(String string) {
        isString = true;
        assert string.charAt(string.length() - 1) == '\0';
        this.string = string.substring(0, string.length() - 1);
    }

    public void setInt(int word) {
        isString = true;
        this.word = word;
    }

    public void setBool(int boolByte) {
        isBool = true;
        this.boolByte = boolByte;
    }
}
