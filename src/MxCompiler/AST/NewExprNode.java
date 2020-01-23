package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class NewExprNode extends ExprNode {
    private String typeName;
    private ArrayList<ExprNode> exprForDim;
    private int dim;

    public NewExprNode(Location location, String typeName, ArrayList<ExprNode> exprForDim, int dim) {
        super(location);
        this.typeName = typeName;
        this.exprForDim = exprForDim;
        this.dim = dim;
    }

    public String getTypeName() {
        return typeName;
    }

    public ArrayList<ExprNode> getExprForDim() {
        return exprForDim;
    }

    public int getDim() {
        return dim;
    }

    public void addExprForDim(ExprNode expr) {
        exprForDim.add(expr);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
