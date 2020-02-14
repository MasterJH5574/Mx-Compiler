package MxCompiler.IR.Operand;

import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.TypeSystem.PointerType;

public class ConstNull extends Constant {
    public ConstNull() {
        super(new PointerType(null));
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
