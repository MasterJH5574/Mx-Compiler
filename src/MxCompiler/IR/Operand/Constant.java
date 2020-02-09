package MxCompiler.IR.Operand;

import MxCompiler.IR.TypeSystem.IRType;

abstract public class Constant extends Operand {
    public Constant(IRType type) {
        super(type);
    }

    @Override
    public boolean isConstValue() {
        return true;
    }
}
