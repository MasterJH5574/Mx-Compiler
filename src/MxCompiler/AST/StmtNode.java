package MxCompiler.AST;

import MxCompiler.Utilities.Location;

abstract public class StmtNode extends ASTNode {
    public StmtNode(Location location) {
        super(location);
    }
}
