package MxCompiler.Entity;

import MxCompiler.Utilities.Location;

abstract public class Entity {
    private String name;
    private boolean referred;
    private Location location;

    public Entity(String name, Location location) {
        this.name = name;
        this.referred = false;
        this.location = location;
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

    public Location getLocation() {
        return location;
    }
}
