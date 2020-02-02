package MxCompiler.Type;

// Only used for method call.

public class MethodType extends Type {
    private ClassType classType;

    public MethodType(String name, ClassType classType) {
        super(name, 0);
        this.classType = classType;
    }

    public ClassType getClassType() {
        return classType;
    }

    @Override
    public String toString() {
        return "#MethodType#";
    }
}
