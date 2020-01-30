package MxCompiler.Entity;

abstract public class Entity {
    private String name;
    private boolean referred;

    public Entity(String name) {
        this.name = name;
        this.referred = false;
    }

    public String getName() {
        return name;
    }

    public boolean isReferred() { // used for warning
        return referred;
    }

    public void setReferred() {
        referred = true;
    }
}
