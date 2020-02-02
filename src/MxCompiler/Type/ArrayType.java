package MxCompiler.Type;

public class ArrayType extends Type {
    private Type baseType;
    private int dims;


    public ArrayType(Type baseType, int dims) {
        super(baseType.getName(), 0);
        this.baseType = baseType;
        this.dims = dims;
    }

    public Type getBaseType() {
        return baseType;
    }

    public int getDims() {
        return dims;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayType)
            return baseType.equals(((ArrayType) obj).baseType) && dims == ((ArrayType) obj).dims;
        else
            return false;
    }

    @Override
    public String toString() {
        return getName() + "[]".repeat(dims);
    }
}
