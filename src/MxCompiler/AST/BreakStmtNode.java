package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class BreakStmtNode extends StmtNode {
    public BreakStmtNode(Location location) {
        super(location);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
