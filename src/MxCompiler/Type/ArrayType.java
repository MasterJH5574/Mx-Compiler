package MxCompiler.Type;

import MxCompiler.AST.PrimitiveTypeNode;
import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class ArrayType extends Type {
    private Type baseType;
    private int dims;

    private ArrayList<FunctionEntity> methods;

    public ArrayType(Type baseType, int dims) {
        super(baseType.getName(), 0);
        this.baseType = baseType;
        this.dims = dims;

        // Add built-in method.
        methods = new ArrayList<>();
        Location location = new Location(0, 0);
        ArrayList<VariableEntity> parameters;
        FunctionEntity method;

        // int size();
        parameters = new ArrayList<>();
        method = new FunctionEntity("size", location,
                new PrimitiveTypeNode(location, "int"), parameters, null,
                FunctionEntity.EntityType.method);
        methods.add(method);
    }

    public Type getBaseType() {
        return baseType;
    }

    public int getDims() {
        return dims;
    }

    public boolean hasMethod(String name) {
        for (FunctionEntity method : methods)
            if (method.getName().equals(name))
                return true;
        return false;
    }

    public FunctionEntity getMethod(String name) {
        for (FunctionEntity method : methods)
            if (method.getName().equals(name))
                return method;
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArrayType)
            return baseType.equals(((ArrayType) obj).baseType) && dims == ((ArrayType) obj).dims;
        else
            return false;
    }

    @Override
    public String toString() {
        return getName() + "[]".repeat(dims);
    }
}
