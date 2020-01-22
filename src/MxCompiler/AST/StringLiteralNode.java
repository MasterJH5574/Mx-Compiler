package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class StringLiteralNode extends ConstExprNode {
    private String value;

    public StringLiteralNode(Location location, String value) {
        super(location);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
