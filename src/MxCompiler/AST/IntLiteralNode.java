package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class IntLiteralNode extends ConstExprNode {
    private int value;

    public IntLiteralNode(Location location, int value) {
        super(location);
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
