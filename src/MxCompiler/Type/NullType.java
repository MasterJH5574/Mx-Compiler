package MxCompiler.Type;

import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IRTypeTable;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.VoidType;

public class NullType extends Type {
    public NullType() {
        super("null", 0);
    }

    @Override
    public IRType getIRType(IRTypeTable irTypeTable) {
        // A Pointer to class/array type.
        return new PointerType(new VoidType());
    }

    @Override
    public Operand getDefaultValue() {
        return new ConstNull();
    }
}
