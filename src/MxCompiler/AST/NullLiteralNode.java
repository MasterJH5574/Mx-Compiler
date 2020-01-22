package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class NullLiteralNode extends ConstExprNode {
    public NullLiteralNode(Location location) {
        super(location);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
