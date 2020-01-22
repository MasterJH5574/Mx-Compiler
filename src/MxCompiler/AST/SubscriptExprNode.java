package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class SubscriptExprNode extends ExprNode {
    private ExprNode name, index;

    public SubscriptExprNode(Location location, ExprNode name, ExprNode index) {
        super(location);
        this.name = name;
        this.index = index;
    }

    public ExprNode getName() {
        return name;
    }

    public ExprNode getIndex() {
        return index;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
