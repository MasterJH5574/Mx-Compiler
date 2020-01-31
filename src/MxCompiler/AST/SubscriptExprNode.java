package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class SubscriptExprNode extends ExprNode {
    private ExprNode name, index;

    public SubscriptExprNode(Location location, String text, ExprNode name, ExprNode index) {
        super(location, text);
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
