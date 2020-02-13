package MxCompiler.IR.TypeSystem;

import MxCompiler.IR.IRVisitor;

public class PointerType extends IRType {
    private IRType baseType;

    public PointerType(IRType baseType) {
        this.baseType = baseType;
    }

    public IRType getBaseType() {
        return baseType;
    }

    @Override
    public int getBytes() {
        return 8;
    }

    @Override
    public String toString() {
        return baseType.toString() + "*";
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
