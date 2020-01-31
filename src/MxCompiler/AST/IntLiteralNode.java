package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class IntLiteralNode extends ConstExprNode {
    private int value;

    public IntLiteralNode(Location location, String text, int value) {
        super(location, text);
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
