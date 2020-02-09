package MxCompiler.IR.TypeSystem;

public class ArrayType extends IRType {
    private int size;
    private IRType type;

    public ArrayType(int size, IRType type) {
        this.size = size;
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public IRType getType() {
        return type;
    }
}
