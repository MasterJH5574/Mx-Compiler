package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class PrefixExprNode extends ExprNode {
    public enum Operator {
        preInc, preDec,
        signPos, signNeg,
        logicalNot, bitwiseComplement
    }

    private Operator op;
    private ExprNode expr;

    public PrefixExprNode(Location location, String text, Operator op, ExprNode expr) {
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
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
