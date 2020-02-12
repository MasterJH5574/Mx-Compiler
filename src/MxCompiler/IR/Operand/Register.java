package MxCompiler.IR.Operand;

import MxCompiler.IR.TypeSystem.IRType;

public class Register extends Operand {
    private String name;

    public Register(IRType type, String name) {
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
}