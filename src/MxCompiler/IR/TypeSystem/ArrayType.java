package MxCompiler.IR.TypeSystem;

import MxCompiler.IR.IRVisitor;

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

    @Override
    public int getBytes() {
        return type.getBytes() * size;
    }

    @Override
    public String toString() {
        return "[" + size + " x " + type.toString() + "]";
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
