package MxCompiler.Entity;

import MxCompiler.AST.ExprNode;
import MxCompiler.AST.PrimitiveTypeNode;
import MxCompiler.AST.TypeNode;
import MxCompiler.Utilities.Location;

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

    public static VariableEntity newEntity(String identifier, String typeName) {
        Location location = new Location(0, 0);
        return new VariableEntity(identifier,
                new PrimitiveTypeNode(location, typeName),
                null, VariableEntity.EntityType.parameter);
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
