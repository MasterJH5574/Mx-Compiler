package MxCompiler.RISCV.Operand.Register;

public class VirtualRegister extends Register {
    private String name;

    public VirtualRegister(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
