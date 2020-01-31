package MxCompiler.AST;

import MxCompiler.Utilities.Location;

abstract public class ExprNode extends ASTNode {
    private String text;

    public ExprNode(Location location, String text) {
        super(location);
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
