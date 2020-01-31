package MxCompiler.AST;

import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class FunctionNode extends ProgramUnitNode {
    private TypeNode type;
    private String identifier;
    private ArrayList<VarNode> parameters;
    private StmtNode statement; // This must be a block statement.

    public FunctionNode(Location location, TypeNode type, String identifier,
                        ArrayList<VarNode> parameters, StmtNode statement) {
        super(location);
        this.type = type;
        this.identifier = identifier;
        this.parameters = parameters;
        this.statement = statement;
    }

    public TypeNode getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ArrayList<VarNode> getParameters() {
        return parameters;
    }

    public void addParameter(VarNode parameter) {
        parameters.add(parameter);
    }

    public StmtNode getStatement() {
        return statement;
    }

    public FunctionEntity getEntity(FunctionEntity.EntityType entityType) {
        ArrayList<VariableEntity> parameters = new ArrayList<>();
        for (VarNode varNode : this.parameters)
            parameters.add(varNode.getEntity(VariableEntity.EntityType.parameter));
        return new FunctionEntity(identifier, type, parameters, statement, entityType);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("<FunctionNode>\n");
        string.append("returnType:\n").append(type.toString());
        string.append("identifier = ").append(identifier);
        string.append("parameter:\n");
        for (VarNode parameter : parameters)
            string.append(parameter.toString());
        string.append("statements:\n").append(statement.toString());
        return string.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
