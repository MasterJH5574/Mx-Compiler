package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class FunctionNode extends ProgramUnitNode {
    private TypeNode type;
    private String identifier;
    private ArrayList<VarNode> parameters = new ArrayList<>();
    private StmtNode statement; // This must be a block statement.

    public FunctionNode(Location location, TypeNode type, String identifier, StmtNode statement) {
        super(location);
        this.type = type;
        this.identifier = identifier;
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

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
