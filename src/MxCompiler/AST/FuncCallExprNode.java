package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class FuncCallExprNode extends ExprNode {
    private ExprNode funcName;
    private ArrayList<ExprNode> parameters;

    public FuncCallExprNode(Location location, ExprNode funcName, ArrayList<ExprNode> parameters) {
        super(location);
        this.funcName = funcName;
        this.parameters = parameters;
    }

    public ExprNode getFuncName() {
        return funcName;
    }

    public void setFuncName(ExprNode funcName) {
        this.funcName = funcName;
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
