package MxCompiler.RISCV.Operand.Address;

public class StackLocation extends Address {
    private String name;

    public StackLocation(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }
}
