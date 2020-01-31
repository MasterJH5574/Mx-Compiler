package MxCompiler.AST;

import MxCompiler.Entity.VariableEntity;
import MxCompiler.Utilities.Location;

public class VarNode extends ProgramUnitNode {
    private TypeNode type;
    private String identifier;
    private ExprNode initExpr;

    public VarNode(Location location, TypeNode type, String identifier, ExprNode initExpr) {
        super(location);
        this.type = type;
        this.identifier = identifier;
        this.initExpr = initExpr; // If initExpr == null, let it be.
    }

    public TypeNode getType() {
        return type;
    }

    public void setType(TypeNode type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean hasInitExpr() {
        return initExpr != null;
    }

    public ExprNode getInitExpr() {
        return initExpr;
    }

    public void setInitExpr(ExprNode initExpr) {
        this.initExpr = initExpr;
    }

    public VariableEntity getEntity(VariableEntity.EntityType entityType) {
        return new VariableEntity(identifier, type, initExpr, entityType);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("<VarNode>\n");
        string.append("type:\n").append(type.toString());
        string.append("identifier = ").append(identifier).append("\n");
        if (hasInitExpr())
            string.append("initExpr:\n").append(initExpr.toString());
        return string.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
