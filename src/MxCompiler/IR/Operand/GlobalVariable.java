package MxCompiler.IR.Operand;

import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.TypeSystem.IRType;

public class GlobalVariable extends Operand {
    private String name;
    private Operand init;

    public GlobalVariable(IRType type, String name, Operand init) {
        super(type);
        this.name = name;
        this.init = init;
    }

    public String getName() {
        return name;
    }

    public Operand getInit() {
        return init;
    }

    public void setInit(Operand init) {
        this.init = init;
    }

    @Override
    public boolean isConstValue() {
        return false;
    }

    public String definitionToString() {
        StringBuilder string = new StringBuilder(toString() + " = ");
        assert init instanceof Constant;
        if (init instanceof ConstString)
            string.append("private unnamed_addr constant ").
                    append(getType().toString()).append(" ").append(init.toString());
        else
            string.append("global ").append(getType().toString()).append(" ").append(init.toString());
        return string.toString();
    }

    @Override
    public String toString() {
        return "@" + name;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
