package MxCompiler.IR.Operand;

import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.VoidType;

public class ConstNull extends Constant {
    public ConstNull() {
        super(new PointerType(new VoidType()));
    }

    @Override
    public Constant castToType(IRType objectType) {
        if (objectType instanceof PointerType)
            return new ConstNull();
        throw new RuntimeException("ConstNull cast to " + objectType.toString());
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConstNull;
    }
}
