package MxCompiler.Type;

import MxCompiler.IR.Operand.ConstInt;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IRTypeTable;
import MxCompiler.IR.TypeSystem.IntegerType;

public class BoolType extends Type {
    public BoolType() {
        super("bool", 0);
    }

    @Override
    public IRType getIRType(IRTypeTable irTypeTable) {
        return irTypeTable.get(this);
    }

    @Override
    public Operand getDefaultValue() {
        return new ConstInt(IntegerType.BitWidth.int1, 0);
    }
}
