package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.Location;

public class BinaryExprNode extends ExprNode {
    public enum Operator {
        mul, div, mod,
        add, sub,
        shiftLeft, shiftRight,
        less, greater, lessEqual, greaterEqual,
        equal, notEqual,
        bitwiseAnd, bitwiseXor, bitwiseOr,
        logicalAnd, logicalOr,
        assign
    }

    private Operator op;
    private ExprNode lhs, rhs;

    public BinaryExprNode(Location location, String text, Operator op, ExprNode lhs, ExprNode rhs) {
        super(location, text);
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Operator getOp() {
        return op;
    }

    public ExprNode getLhs() {
        return lhs;
    }

    public ExprNode getRhs() {
        return rhs;
    }

    @Override
    public String toString() {
        String headString = "<BinaryExprNode>\n";
        String opString = "op = " + op.toString();
        String lhsString = "lhs = " + lhs.toString();
        String rhsString = "rhs = " + rhs.toString();
        return headString + opString + lhsString + rhsString;
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }
}
