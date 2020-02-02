package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
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

    public boolean hasInit() {
        return init != null;
    }

    public ExprNode getInit() {
        return init;
    }

    public boolean hasCond() {
        return cond != null;
    }

    public ExprNode getCond() {
        return cond;
    }

    public boolean hasStep() {
        return step != null;
    }

    public ExprNode getStep() {
        return step;
    }

    public StmtNode getBody() {
        return body;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("<ForStmtNode>\n");
        if (hasInit())
            string.append("init = ").append(init.toString());
        if (hasCond())
            string.append("cond = ").append(cond.toString());
        if (hasStep())
            string.append("step = ").append(step.toString());
        string.append("body = ").append(body.toString());
        return string.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }
}
