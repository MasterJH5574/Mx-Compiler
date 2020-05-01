package MxCompiler.RISCV.Operand;

import MxCompiler.RISCV.ASMVisitor;

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

    public String getName() {
        return name;
    }

    public void setString(String string) {
        isString = true;
        this.string = string;
        this.name = ".L" + this.name;
    }

    public boolean isString() {
        return isString;
    }

    public void setInt(int word) {
        isInt = true;
        this.word = word;
    }

    public void setBool(int boolByte) {
        isBool = true;
        this.boolByte = boolByte;
    }

    @Override
    public String emitCode() {
        if (isBool)
            return "\t.byte\t" + boolByte + " ".repeat(36 - 13) + "# " + (boolByte == 1);
        else if (isInt) {
            return "\t.word\t" + Integer.toUnsignedLong(word)
                    + " ".repeat(24 - Integer.toUnsignedString(word).length()) + "# " + word;
        } else if (isString) {
            String res = string.replace("\\", "\\\\");
            res = res.replace("\n", "\\n");
            res = res.replace("\"", "\\\"");
            return "\t.asciz\t\"" + res + "\"";
        } else
            throw new RuntimeException();
    }

    @Override
    public String toString() {
        return name;
    }

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
