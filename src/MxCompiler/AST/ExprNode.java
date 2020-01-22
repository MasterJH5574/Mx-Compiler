package MxCompiler.AST;

import MxCompiler.Utilities.Location;

abstract public class ExprNode extends ASTNode {
    public ExprNode(Location location) {
        super(location);
    }
}
