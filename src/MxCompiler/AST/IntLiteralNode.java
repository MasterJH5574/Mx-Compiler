package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class IntLiteralNode extends ConstExprNode {
    private long value;

    public IntLiteralNode(Location location, String text, long value) {
        super(location, text);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "<IntLiteralNode>\n" + "value = " + value + "\n";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
