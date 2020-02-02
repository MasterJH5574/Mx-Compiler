package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.Location;

public class IdExprNode extends ExprNode {
    private String identifier;

    public IdExprNode(Location location, String text, String identifier) {
        super(location, text);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return "<IdExprNode>\n" + "identifier = " + identifier + "\n";
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }
}
