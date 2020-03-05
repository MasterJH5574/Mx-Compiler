package MxCompiler.IR.Operand;

import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.TypeSystem.IRType;

import java.util.Queue;
import java.util.Set;

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

    public String getFullName() {
        return toString();
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
    public void markBaseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        // do nothing.
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
}
