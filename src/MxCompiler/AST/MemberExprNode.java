package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
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
    public String toString() {
        return "<MemberExprNode>\n" + "expr:\n" + expr.toString() + "identifier = " + identifier + "\n";
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }
}
