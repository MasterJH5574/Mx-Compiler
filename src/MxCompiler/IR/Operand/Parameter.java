package MxCompiler.IR.Operand;

import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.TypeSystem.IRType;

public class Parameter extends Operand {
    private String name;

    public Parameter(IRType type, String name) {
        super(type);
        this.name = name;
    }

    public String getName() {
            return name;
    }

    public String getNameWithoutDot() {
        if (name.contains("."))
            return name.split(".")[0];
        else
            throw new RuntimeException();
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isConstValue() {
        return false;
    }

    @Override
    public String toString() {
        return "%" + name;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
