package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.Location;

public class BreakStmtNode extends StmtNode {
    public BreakStmtNode(Location location) {
        super(location);
    }

    @Override
    public String toString() {
        return "<BreakStmtNode>\n";
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }
}
