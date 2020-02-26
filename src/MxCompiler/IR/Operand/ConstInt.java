package MxCompiler.IR.Operand;

import MxCompiler.IR.TypeSystem.IRType;
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

    @Override
    public Constant castToType(IRType objectType) {
        if (objectType instanceof IntegerType) {
            if (((IntegerType) objectType).getBitWidth() == IntegerType.BitWidth.int1)
                return new ConstBool(value == 0);
            else if (((IntegerType) objectType).getBitWidth() == IntegerType.BitWidth.int8)
                return new ConstInt(IntegerType.BitWidth.int8, value);
            else if (((IntegerType) objectType).getBitWidth() == IntegerType.BitWidth.int32)
                return new ConstInt(IntegerType.BitWidth.int32, value);
        }
        throw new RuntimeException("ConstBool cast to " + objectType.toString());
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConstInt && value == ((ConstInt) obj).value;
    }
}
