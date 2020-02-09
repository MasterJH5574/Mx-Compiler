package MxCompiler.Type;

// Only used for method call.

import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IRTypeTable;

public class MethodType extends Type {
    private Type type;

    public MethodType(String name, Type type) {
        super(name, 0);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "#MethodType#";
    }

    @Override
    public IRType getIRType(IRTypeTable irTypeTable) {
        // This method will never be called.
        return null;
    }

    @Override
    public Operand getDefaultValue() {
        // This method will never be called.
        return null;
    }
}
