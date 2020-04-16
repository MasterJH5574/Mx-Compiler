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

    public void setValue(long value) {
        this.value = value;
    }

    public int getPowerOfTwo() {
        // 0: value != 2^k
        // positive: value = 2^k
        // negative: value = -2^k
        boolean neg = this.value < 0;
        long value = neg ? -this.value : this.value;
        int res = 0;
        while (value > 1) {
            if ((value & 1) == 0) {
                res++;
                value >>= 1;
            } else
                return 0;
        }
        return neg ? -res : res;
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
