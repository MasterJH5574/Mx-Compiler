package MxCompiler.IR.Operand;

import MxCompiler.IR.TypeSystem.IntegerType;

public class ConstInt extends Constant {
    private long value;

    public ConstInt(IntegerType.BitWidth bitWidth, long value) {
        super(new IntegerType(bitWidth));
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}
