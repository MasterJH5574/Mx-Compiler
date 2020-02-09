package MxCompiler.IR.Operand;

import MxCompiler.IR.TypeSystem.PointerType;

public class ConstNull extends Constant {
    public ConstNull() {
        super(new PointerType(null));
    }
}
