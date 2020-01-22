package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class FuncCallExprNode extends ExprNode {
    private ExprNode funcName;
    private ArrayList<ExprNode> parameters = new ArrayList<>();

    public FuncCallExprNode(Location location, ExprNode funcName) {
        super(location);
        this.funcName = funcName;
    }

    public ExprNode getFuncName() {
        return funcName;
    }

    public ArrayList<ExprNode> getParameters() {
        return parameters;
    }

    public void addParameter(ExprNode parameter) {
        parameters.add(parameter);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
