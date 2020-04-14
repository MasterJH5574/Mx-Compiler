package MxCompiler.RISCV.Operand.Address;

public class StackLocation extends Address {
    private String name;

    public StackLocation(String name) {
        this.name = name;
    }
}
