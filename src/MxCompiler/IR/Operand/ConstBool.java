package MxCompiler.IR.Operand;

import MxCompiler.IR.TypeSystem.IntegerType;

public class ConstBool extends Constant {
    private boolean value;

    public ConstBool(boolean value) {
        super(new IntegerType(IntegerType.BitWidth.int1));
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }
}