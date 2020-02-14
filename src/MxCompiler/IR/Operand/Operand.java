package MxCompiler.IR.Operand;

import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.TypeSystem.IRType;

abstract public class Operand {
    private IRType type;

    public Operand(IRType type) {
        this.type = type;
    }

    public IRType getType() {
        return type;
    }

    abstract public boolean isConstValue();

    @Override
    abstract public String toString();

    abstract public void accept(IRVisitor visitor);
}
