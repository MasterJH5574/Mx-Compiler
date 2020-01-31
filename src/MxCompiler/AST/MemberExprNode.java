package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class MemberExprNode extends ExprNode {
    private ExprNode expr;
    private String identifier;

    public MemberExprNode(Location location, String text, ExprNode expr, String identifier) {
        super(location, text);
        this.expr = expr;
        this.identifier = identifier;
    }

    public ExprNode getExpr() {
        return expr;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
