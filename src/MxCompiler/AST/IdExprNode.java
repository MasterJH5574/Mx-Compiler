package MxCompiler.AST;

import MxCompiler.Entity.Entity;
import MxCompiler.Utilities.Location;

public class IdExprNode extends ExprNode {
    private String identifier;
    private Entity entity;

    public IdExprNode(Location location, String text, String identifier) {
        super(location, text);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "<IdExprNode>\n" + "identifier = " + identifier + "\n";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
