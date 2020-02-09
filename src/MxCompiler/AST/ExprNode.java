package MxCompiler.AST;

import MxCompiler.Entity.Entity;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.Type.Type;
import MxCompiler.Utilities.Location;

abstract public class ExprNode extends ASTNode {
    private String text;
    private Entity entity;
    private Boolean lvalue;
    private Type type;

    // for IR
    private Operand result;

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

    public void setText(String text) {
        this.text = text;
    }

    public Operand getResult() {
        return result;
    }

    public void setResult(Operand result) {
        this.result = result;
    }

    @Override
    abstract public String toString();
}
