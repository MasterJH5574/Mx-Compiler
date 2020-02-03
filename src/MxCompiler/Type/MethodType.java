package MxCompiler.Type;

// Only used for method call.

public class MethodType extends Type {
    private Type type;

    public MethodType(String name, Type type) {
        super(name, 0);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "#MethodType#";
    }
}
