package MxCompiler.Type;

abstract public class Type {
    private String name;
    private long size;

    public Type(String name, long size) {
        this.name = name;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }
}
