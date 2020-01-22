package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class BoolLiteralNode extends ConstExprNode {
    private boolean value;

    public BoolLiteralNode(Location location, boolean value) {
        super(location);
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
