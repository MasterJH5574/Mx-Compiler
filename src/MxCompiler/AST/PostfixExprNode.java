package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class PostfixExprNode extends ExprNode {
    public enum Operator {
        postInc, postDec
    }

    private Operator op;
    private ExprNode expr;

    public PostfixExprNode(Location location, Operator op, ExprNode expr) {
        super(location);
        this.op = op;
        this.expr = expr;
    }

    public Operator getOp() {
        return op;
    }

    public ExprNode getExpr() {
        return expr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
