package MxCompiler.IR.Operand;

import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IntegerType;

public class ConstBool extends Constant {
    private boolean value;

    public ConstBool(boolean value) {
        super(new IntegerType(IntegerType.BitWidth.int1));
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Constant castToType(IRType objectType) {
        if (objectType instanceof IntegerType) {
            if (((IntegerType) objectType).getBitWidth() == IntegerType.BitWidth.int1)
                return new ConstBool(value);
            else if (((IntegerType) objectType).getBitWidth() == IntegerType.BitWidth.int8)
                return new ConstInt(IntegerType.BitWidth.int8, value ? 1 : 0);
            else if (((IntegerType) objectType).getBitWidth() == IntegerType.BitWidth.int32)
                return new ConstInt(IntegerType.BitWidth.int32, value ? 1 : 0);
        }
        throw new RuntimeException("ConstBool cast to " + objectType.toString());
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConstBool && value == ((ConstBool) obj).value;
    }
}
