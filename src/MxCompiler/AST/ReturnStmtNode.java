package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class ReturnStmtNode extends StmtNode {
    private ExprNode returnValue;

    public ReturnStmtNode(Location location, ExprNode returnValue) {
        super(location);
        this.returnValue = returnValue;
    }

    public ExprNode getReturnValue() {
        return returnValue;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
