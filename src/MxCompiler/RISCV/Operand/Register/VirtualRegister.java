package MxCompiler.RISCV.Operand.Register;

public class VirtualRegister extends Register {
    private String name;

    private boolean colorFixed;
    private PhysicalRegister colorPR;

    public VirtualRegister(String name) {
        this.name = name;

        colorFixed = false;
        colorPR = null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void fixColor(PhysicalRegister pr) {
        colorFixed = true;
        colorPR = pr;
    }
}
