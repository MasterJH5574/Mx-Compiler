package MxCompiler.AST;

import MxCompiler.Frontend.Scope;
import MxCompiler.Utilities.Location;

abstract public class ASTNode {
    private Location location;
    private Scope scope;

    public ASTNode(Location location) {
        this.location = location;
        scope = null;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    abstract public void accept(ASTVisitor visitor);

    @Override
    abstract public String toString();

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ASTNode)
            return toString().equals(obj.toString());
        else
            return false;
    }
}
