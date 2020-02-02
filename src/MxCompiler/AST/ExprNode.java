package MxCompiler.AST;

import MxCompiler.Entity.Entity;
import MxCompiler.Type.Type;
import MxCompiler.Utilities.Location;

abstract public class ExprNode extends ASTNode {
    private String text;
    private Entity entity;
    private Boolean lvalue;
    private Type type;

    public ExprNode(Location location, String text) {
        super(location);
        this.text = text;
        lvalue = null;
        entity = null;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Boolean getLvalue() {
        return lvalue;
    }

    public void setLvalue(boolean lValue) {
        this.lvalue = lValue;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    @Override
    abstract public String toString();
}
