package MxCompiler.IR.TypeSystem;

public class VoidType extends IRType {
    @Override
    public int getBytes() {
        return 0;
    }

    @Override
    public String toString() {
        return "void";
    }
}
