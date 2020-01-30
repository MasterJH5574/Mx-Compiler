package MxCompiler.Type;

import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;

import java.util.ArrayList;

public class ClassType extends Type {
    private ArrayList<VariableEntity> members;
    private FunctionEntity constructor;
    private ArrayList<FunctionEntity> methods;

    public ClassType(String name, ArrayList<VariableEntity> members,
                     FunctionEntity constructor, ArrayList<FunctionEntity> methods) {
        super(name, 0);
        this.members = members;
        this.constructor = constructor;
        this.methods = methods;
    }

    public ArrayList<VariableEntity> getMembers() {
        return members;
    }

    public FunctionEntity getConstructor() {
        return constructor;
    }

    public ArrayList<FunctionEntity> getMethods() {
        return methods;
    }
}
