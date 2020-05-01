package MxCompiler.RISCV.Operand.Immediate;

import MxCompiler.RISCV.Operand.GlobalVariable;

public class RelocationImmediate extends Immediate {
    public enum Type {
        high, low
    }

    private Type type;
    private GlobalVariable globalVariable;

    public RelocationImmediate(Type type, GlobalVariable globalVariable) {
        this.type = type;
        this.globalVariable = globalVariable;
    }

    @Override
    public String emitCode() {
        return "%" + (type == Type.high ? "hi" : "lo") + "(" + globalVariable.getName() + ")";
    }

    @Override
    public String toString() {
        return "%" + (type == Type.high ? "hi" : "lo") + "(" + globalVariable.getName() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RelocationImmediate))
            return false;
        return type == ((RelocationImmediate) obj).type
                && globalVariable == ((RelocationImmediate) obj).globalVariable;
    }
}
