package MxCompiler.IR.TypeSystem;

import MxCompiler.IR.Operand.Operand;

abstract public class IRType {
    abstract public int getBytes();

    abstract public Operand getDefaultValue();

    @Override
    abstract public String toString();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IRType))
            return false;
        else
            return toString().equals(obj.toString());
    }
}
