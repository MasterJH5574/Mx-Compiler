package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class IfStmtNode extends StmtNode {
    private ExprNode cond;
    private StmtNode thenBody, elseBody;

    public IfStmtNode(Location location, ExprNode cond, StmtNode thenBody, StmtNode elseBody) {
        super(location);
        this.cond = cond;
        this.thenBody = thenBody;
        this.elseBody = elseBody;
    }

    public ExprNode getCond() {
        return cond;
    }

    public StmtNode getThenBody() {
        return thenBody;
    }

    public StmtNode getElseBody() {
        return elseBody;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
