package MxCompiler.IR.TypeSystem;

import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.Operand;

public class PointerType extends IRType {
    private IRType baseType;

    public PointerType(IRType baseType) {
        this.baseType = baseType;
    }

    public IRType getBaseType() {
        return baseType;
    }

    @Override
    public Operand getDefaultValue() {
        return new ConstNull();
    }

    @Override
    public int getBytes() {
        return 8;
    }

    @Override
    public String toString() {
        return baseType.toString() + "*";
    }
}
