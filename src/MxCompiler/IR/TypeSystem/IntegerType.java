package MxCompiler.IR.TypeSystem;

import MxCompiler.IR.IRVisitor;

public class IntegerType extends IRType {
    public enum BitWidth {
        int1, int8, int32
    }

    private BitWidth bitWidth;

    public IntegerType(BitWidth bitWidth) {
        this.bitWidth = bitWidth;
    }

    public BitWidth getBitWidth() {
        return bitWidth;
    }

    @Override
    public int getBytes() {
        if (bitWidth == BitWidth.int1 || bitWidth == BitWidth.int8)
            return 1;
        else
            return 4;
    }

    @Override
    public String toString() {
        if (bitWidth == BitWidth.int1)
            return "i1";
        else if (bitWidth == BitWidth.int8)
            return "i8";
        else
            return "i32";
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
