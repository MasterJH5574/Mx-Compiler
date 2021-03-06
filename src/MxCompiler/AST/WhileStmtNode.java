package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.Location;

public class WhileStmtNode extends StmtNode {
    private ExprNode cond;
    private StmtNode body;

    public WhileStmtNode(Location location, ExprNode cond, StmtNode body) {
        super(location);
        this.cond = cond;
        this.body = body;
    }

    public ExprNode getCond() {
        return cond;
    }

    public boolean hasBody() {
        return body != null;
    }

    public StmtNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "<WhileStmtNode>\n" + "cond:\n" + cond.toString() + "body:\n" + body.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }
}
