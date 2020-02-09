package MxCompiler.IR.TypeSystem;

public class PointerType extends IRType {
    private IRType baseType;

    public PointerType(IRType baseType) {
        this.baseType = baseType;
    }

    public IRType getBaseType() {
        return baseType;
    }
}
