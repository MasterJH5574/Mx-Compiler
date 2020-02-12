package MxCompiler.Type;

import MxCompiler.IR.Operand.ConstInt;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IRTypeTable;

public class IntType extends Type {
    public IntType() {
        super("int", 0);
    }

    @Override
    public IRType getIRType(IRTypeTable irTypeTable) {
        return irTypeTable.get(this);
    }

    @Override
    public Operand getDefaultValue() {
        return new ConstInt(0);
    }
}
