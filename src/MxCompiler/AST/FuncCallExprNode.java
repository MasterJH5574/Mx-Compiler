package MxCompiler.AST;

import MxCompiler.Entity.Entity;
import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class FuncCallExprNode extends ExprNode {
    private ExprNode funcName;
    private ArrayList<ExprNode> parameters;
    private Entity entity;

    public FuncCallExprNode(Location location, String text, ExprNode funcName, ArrayList<ExprNode> parameters) {
        super(location, text);
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

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("<FuncCallExprNode>\n");
        string.append("funcName:\n").append(funcName.toString());
        string.append("parameters:\n");
        for (ExprNode parameter : parameters)
            string.append(parameter.toString());
        return string.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
