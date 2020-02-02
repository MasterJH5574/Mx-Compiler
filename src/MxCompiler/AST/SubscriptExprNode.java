package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.Location;

public class SubscriptExprNode extends ExprNode {
    private ExprNode name, index;
    private int dim;

    public SubscriptExprNode(Location location, String text, ExprNode name, ExprNode index) {
        super(location, text);
        this.name = name;
        this.index = index;
        if (name instanceof SubscriptExprNode)
            dim = ((SubscriptExprNode) name).dim + 1;
        else
            dim = 1;
    }

    public ExprNode getName() {
        return name;
    }

    public ExprNode getIndex() {
        return index;
    }

    public int getDim() {
        return dim;
    }

    @Override
    public String toString() {
        return "<SubscriptExprNode>\n" + "name:\n" + name.toString() + "index:\n" + index.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }
}
