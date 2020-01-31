package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class ContinueStmtNode extends StmtNode {
    public ContinueStmtNode(Location location) {
        super(location);
    }

    @Override
    public String toString() {
        return "<ContinueStmtNode>\n";
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
