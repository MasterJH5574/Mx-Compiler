package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class ForStmtNode extends StmtNode {
    private ExprNode init, cond, step;
    private StmtNode body;

    public ForStmtNode(Location location, ExprNode init, ExprNode cond, ExprNode step, StmtNode body) {
        super(location);
        this.init = init;
        this.cond = cond;
        this.step = step;
        this.body = body;
    }

    public ExprNode getInit() {
        return init;
    }

    public ExprNode getCond() {
        return cond;
    }

    public ExprNode getStep() {
        return step;
    }

    public StmtNode getBody() {
        return body;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
