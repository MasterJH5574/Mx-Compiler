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
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
