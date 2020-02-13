package MxCompiler.IR.TypeSystem;

import MxCompiler.IR.IRVisitor;

abstract public class IRType {
    abstract public int getBytes();

    @Override
    abstract public String toString();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IRType))
            return false;
        else
            return toString().equals(obj.toString());
    }

    abstract public void accept(IRVisitor visitor);
}
