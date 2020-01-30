package MxCompiler.Entity;

import MxCompiler.AST.ExprNode;
import MxCompiler.AST.TypeNode;

public class VariableEntity extends Entity {
    public enum EntityType {
        global, local, parameter, member
    }

    private TypeNode type;
    private ExprNode initExpr;
    private EntityType entityType;

    public VariableEntity(String name, TypeNode type, ExprNode initExpr, EntityType entityType) {
        super(name);
        this.type = type;
        this.initExpr = initExpr;
        this.entityType = entityType;
    }

    public TypeNode getType() {
        return type;
    }

    public ExprNode getInitExpr() {
        return initExpr;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
