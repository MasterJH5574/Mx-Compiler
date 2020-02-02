package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.Location;

public class PostfixExprNode extends ExprNode {
    public enum Operator {
        postInc, postDec
    }

    private Operator op;
    private ExprNode expr;

    public PostfixExprNode(Location location, String text, Operator op, ExprNode expr) {
        super(location, text);
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
    public String toString() {
        return "<PostfixExprNode>\n" + "op = " + op + "\nexpr:\n" + expr.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }
}
