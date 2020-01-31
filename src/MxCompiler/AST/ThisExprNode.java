package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class ThisExprNode extends ExprNode {
    public ThisExprNode(Location location, String text) {
        super(location, text);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
