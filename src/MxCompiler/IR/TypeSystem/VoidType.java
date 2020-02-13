package MxCompiler.IR.TypeSystem;

import MxCompiler.IR.IRVisitor;

public class VoidType extends IRType {
    @Override
    public int getBytes() {
        return 0;
    }

    @Override
    public String toString() {
        return "void";
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
