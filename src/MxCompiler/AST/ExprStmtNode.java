package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class ExprStmtNode extends StmtNode {
    private ExprNode expr;

    public ExprStmtNode(Location location, ExprNode expr) {
        super(location);
        this.expr = expr;
    }

    public ExprNode getExpr() {
        return expr;
    }

    @Override
    public String toString() {
        return "<ExprStmtNode>\n" + expr.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
