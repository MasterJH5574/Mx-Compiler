package MxCompiler.IR.Operand;

import MxCompiler.IR.TypeSystem.IntegerType;

public class ConstInt extends Constant {
    private long value;

    public ConstInt(long value) {
        super(new IntegerType(IntegerType.BitWidth.int32));
        this.value = value;
    }

    public long getValue() {
        return value;
    }
}