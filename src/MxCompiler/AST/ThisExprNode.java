package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.Location;

public class ThisExprNode extends ExprNode {
    public ThisExprNode(Location location, String text) {
        super(location, text);
    }

    @Override
    public String toString() {
        return "<ThisExprNode>\n";
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }
}
