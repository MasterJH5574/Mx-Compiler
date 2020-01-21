package MxCompiler.AST;

import MxCompiler.Utils.Location;

abstract public class StmtNode extends ASTNode {
    public StmtNode(Location location) {
        super(location);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
