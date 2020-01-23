package MxCompiler.AST;

import MxCompiler.Utilities.Location;

abstract public class ASTNode {
    private Location location;

    public ASTNode(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    abstract public void accept(ASTVisitor visitor);
}
