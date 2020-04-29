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
    public boolean equals(Object obj) {
        if (!(obj instanceof RelocationImmediate))
            return false;
        return type == ((RelocationImmediate) obj).type
                && globalVariable == ((RelocationImmediate) obj).globalVariable;
    }
}
