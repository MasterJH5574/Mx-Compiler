package MxCompiler.IR.Operand;

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
}
