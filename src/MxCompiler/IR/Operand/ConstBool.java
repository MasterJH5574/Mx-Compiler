package MxCompiler.IR.Operand;

import MxCompiler.IR.TypeSystem.IRType;

public class ConstBool extends Constant {
    private boolean value;

    public ConstBool(IRType type, boolean value) {
        super(type);
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }
}
