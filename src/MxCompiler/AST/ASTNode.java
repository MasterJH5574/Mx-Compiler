package MxCompiler.AST;

import MxCompiler.Utils.Location;

abstract public class ASTNode {
    private Location location;

    public ASTNode(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    abstract public void accept(ASTVisitor visitor);
}
