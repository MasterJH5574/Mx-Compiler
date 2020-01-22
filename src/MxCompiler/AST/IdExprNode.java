package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class IdExprNode extends ExprNode {
    private String identifier;

    public IdExprNode(Location location, String identifier) {
        super(location);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
