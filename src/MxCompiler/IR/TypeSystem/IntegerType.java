package MxCompiler.IR.TypeSystem;

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
}
