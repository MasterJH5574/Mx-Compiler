package MxCompiler.IR.Operand;

import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.TypeSystem.IRType;

public class ConstString extends Constant {
    private String value;

    public ConstString(IRType type, String value) {
        super(type);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        String text = value;
        text = text.replace("\n", "\\0A");
        text = text.replace("\t", "\\09");
        text = text.replace("\"", "\\22");
        text = text.replace("\0", "\\00");

        return "c\"" + text + "\"";
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
