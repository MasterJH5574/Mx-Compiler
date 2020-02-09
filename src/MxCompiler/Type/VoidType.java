package MxCompiler.Type;

import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IRTypeTable;

public class VoidType extends Type {
    public VoidType() {
        super("void", 0);
    }

    @Override
    public IRType getIRType(IRTypeTable irTypeTable) {
        return irTypeTable.get(this);
    }

    @Override
    public Operand getDefaultValue() {
        // This method will never be called.
        return null;
    }
}
