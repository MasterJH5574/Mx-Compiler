package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class BoolLiteralNode extends ConstExprNode {
    private boolean value;

    public BoolLiteralNode(Location location, String text, boolean value) {
        super(location, text);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "<BoolLiteralNode>\n" + "value = " + value + "\n";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
