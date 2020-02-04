package MxCompiler.Entity;

import MxCompiler.AST.StmtNode;
import MxCompiler.AST.TypeNode;
import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class FunctionEntity extends Entity {
    public enum EntityType {
        function, method, constructor
    }

    private TypeNode returnType;
    private ArrayList<VariableEntity> parameters;
    private StmtNode bodyStmt;
    private EntityType entityType;

    public FunctionEntity(String name, Location location, TypeNode returnType,
                          ArrayList<VariableEntity> parameters, StmtNode bodyStmt, EntityType entityType) {
        super(name, location);
        this.returnType = returnType;
        this.parameters = parameters;
        this.bodyStmt = bodyStmt;
        this.entityType = entityType;
    }

    public TypeNode getReturnType() {
        return returnType;
    }

    public ArrayList<VariableEntity> getParameters() {
        return parameters;
    }

    public StmtNode getBodyStmt() {
        return bodyStmt;
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
