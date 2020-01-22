package MxCompiler.AST;

import MxCompiler.Utilities.Location;

abstract public class TypeNode extends ASTNode {
    protected String identifier;

    public TypeNode(Location location, String identifier) {
        super(location);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }
}
