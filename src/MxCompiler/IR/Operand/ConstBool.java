package MxCompiler.IR.Operand;

import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.TypeSystem.IntegerType;

public class ConstBool extends Constant {
    private boolean value;

    public ConstBool(boolean value) {
        super(new IntegerType(IntegerType.BitWidth.int1));
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
