package MxCompiler.Type;

public class ArrayType extends Type {
    private Type baseType;
    private int dims;


    public ArrayType(String name, Type baseType, int dims) {
        super(name, 0);
        this.baseType = baseType;
        this.dims = dims;
    }

    public Type getBaseType() {
        return baseType;
    }

    public int getDims() {
        return dims;
    }
}
