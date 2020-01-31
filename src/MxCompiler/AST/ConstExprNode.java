package MxCompiler.AST;

import MxCompiler.Utilities.Location;

abstract public class ConstExprNode extends ExprNode {
    public ConstExprNode(Location location, String text) {
        super(location, text);
    }
}
