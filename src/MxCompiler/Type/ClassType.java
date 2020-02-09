package MxCompiler.Type;

import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IRTypeTable;
import MxCompiler.IR.TypeSystem.PointerType;

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

    public boolean hasMember(String name) {
        for (VariableEntity member : members)
            if (member.getName().equals(name))
                return true;
        return false;
    }

    public VariableEntity getMember(String name) {
        for (VariableEntity member : members)
            if (member.getName().equals(name))
                return member;
        return null;
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

    public boolean hasMemberOrMethod(String name) {
        return hasMember(name) || hasMethod(name);
    }

    @Override
    public IRType getIRType(IRTypeTable irTypeTable) {
        IRType baseType = irTypeTable.get(this);
        return new PointerType(baseType);
    }

    @Override
    public Operand getDefaultValue() {
        return new ConstNull();
    }
}
