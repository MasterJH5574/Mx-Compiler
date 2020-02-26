package MxCompiler.IR.TypeSystem;

import MxCompiler.IR.Operand.Operand;

public class VoidType extends IRType {
    @Override
    public int getBytes() {
        return 0;
    }

    @Override
    public Operand getDefaultValue() {
        // This method will never be called.
        throw new RuntimeException();
    }

    @Override
    public String toString() {
        return "void";
    }
}
