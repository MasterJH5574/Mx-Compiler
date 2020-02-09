package MxCompiler.Type;

import MxCompiler.AST.PrimitiveTypeNode;
import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IRTypeTable;
import MxCompiler.IR.TypeSystem.IntegerType;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class StringType extends Type {
    private ArrayList<FunctionEntity> methods;

    public StringType() {
        super("string", 0);

        // Add built-in methods.
        methods = new ArrayList<>();

        Location location = new Location(0, 0);
        ArrayList<VariableEntity> parameters;
        FunctionEntity method;

        // int length();
        parameters = new ArrayList<>();
        method = new FunctionEntity("length", location,
                new PrimitiveTypeNode(location, "int"), parameters, null,
                FunctionEntity.EntityType.method);
        methods.add(method);

        // string substring(int left, int right);
        parameters = new ArrayList<>();
        parameters.add(VariableEntity.newEntity("left", "int"));
        parameters.add(VariableEntity.newEntity("right", "int"));
        method = new FunctionEntity("substring", location,
                new PrimitiveTypeNode(location, "string"), parameters, null,
                FunctionEntity.EntityType.method);
        methods.add(method);

        // int parseInt();
        parameters = new ArrayList<>();
        method = new FunctionEntity("parseInt", location,
                new PrimitiveTypeNode(location, "int"), parameters, null,
                FunctionEntity.EntityType.method);
        methods.add(method);

        // int ord(int pos);
        parameters = new ArrayList<>();
        parameters.add(VariableEntity.newEntity("pos", "int"));
        method = new FunctionEntity("ord", location,
                new PrimitiveTypeNode(location, "int"), parameters, null,
                FunctionEntity.EntityType.method);
        methods.add(method);
    }

    public ArrayList<FunctionEntity> getMethods() {
        return methods;
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
    public IRType getIRType(IRTypeTable irTypeTable) {
        return irTypeTable.get(this);
    }

    @Override
    public Operand getDefaultValue() {
        return new ConstNull();
    }
}
