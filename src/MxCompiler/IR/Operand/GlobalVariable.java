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

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
