package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class NewExprNode extends ExprNode {
    private TypeNode baseType;
    private ArrayList<ExprNode> exprForDim;
    private int dim;

    public NewExprNode(Location location, String text, TypeNode baseType, ArrayList<ExprNode> exprForDim, int dim) {
        super(location, text);
        this.baseType = baseType;
        this.exprForDim = exprForDim;
        this.dim = dim;
    }

    public TypeNode getBaseType() {
        return baseType;
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
