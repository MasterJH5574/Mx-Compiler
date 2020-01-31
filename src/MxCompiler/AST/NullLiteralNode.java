package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class NullLiteralNode extends ConstExprNode {
    public NullLiteralNode(Location location, String text) {
        super(location, text);
    }

    @Override
    public String toString() {
        return "<NullLiteralNode>\n";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
