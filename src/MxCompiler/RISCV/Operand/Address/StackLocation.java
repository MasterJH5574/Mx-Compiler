package MxCompiler.RISCV.Operand.Address;

public class StackLocation extends Address {
    private String name;
    private int offset;

    public StackLocation(String name) {
        this.name = name;
        offset = -1;
    }

    public void setOffset(int offset) {
        assert this.offset == -1;
        this.offset = offset;
    }

    @Override
    public String emitCode() {
        return offset + "(sp)";
    }

    @Override
    public String toString() {
        return name + "(sp)";
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
