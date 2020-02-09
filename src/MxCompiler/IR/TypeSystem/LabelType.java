package MxCompiler.IR.TypeSystem;

public class LabelType extends IRType {
    private String name;

    public LabelType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
