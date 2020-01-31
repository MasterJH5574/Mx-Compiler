package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class StringLiteralNode extends ConstExprNode {
    private String value;

    public StringLiteralNode(Location location, String text, String value) {
        super(location, text);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "<StringLiteralNode>\n" + "value = " + value + "\n";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
