package MxCompiler.RISCV.Operand;

public class RelocationExpansion extends ASMOperand {
    private enum Type {
        high, low
    }

    private Type type;
    private GlobalVariable globalVariable;

    public RelocationExpansion(Type type, GlobalVariable globalVariable) {
        this.type = type;
        this.globalVariable = globalVariable;
    }
}
