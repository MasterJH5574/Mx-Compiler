package MxCompiler.Frontend;

import MxCompiler.AST.*;
import MxCompiler.Entity.Entity;
import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.Type.Type;
import MxCompiler.Type.TypeTable;
import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.ErrorHandler;
import MxCompiler.Utilities.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Scope {
    public enum ScopeType {
        programScope, classScope, functionScope, blockScope, loopScope
    }

    private Scope parentScope;
    private ArrayList<Scope> childrenScope;

    private Map<String, Entity> entities;
    private ScopeType scopeType;
    private TypeNode functionReturnType;
    private Type classType;

    public Scope(Scope parentScope, ScopeType scopeType, TypeNode functionReturnType, Type classType) {
        this.parentScope = parentScope;
        if (parentScope != null)
            parentScope.childrenScope.add(this);
        this.childrenScope = new ArrayList<>();
        this.entities = new HashMap<>();
        this.scopeType = scopeType;
        this.functionReturnType = functionReturnType;
        this.classType = classType;
    }

    public Scope getParentScope() {
        return parentScope;
    }

    public ArrayList<Scope> getChildrenScope() {
        return childrenScope;
    }

    public Map<String, Entity> getEntities() {
        return entities;
    }

    public ScopeType getScopeType() {
        return scopeType;
    }

    public TypeNode getFunctionReturnType() {
        return functionReturnType;
    }

    public Type getClassType() {
        return classType;
    }

    public void addBuiltInFunction() {
        Location location = new Location(0, 0);
        ArrayList<VariableEntity> parameters;
        FunctionEntity function;

        // void print(string str);
        parameters = new ArrayList<>();
        parameters.add(VariableEntity.newEntity("str", "string"));
        function = new FunctionEntity("print", location,
                new PrimitiveTypeNode(location, "void"), parameters, null,
                FunctionEntity.EntityType.function);
        entities.put("print", function);

        // void println(string str);
        parameters = new ArrayList<>();
        parameters.add(VariableEntity.newEntity("str", "string"));
        function = new FunctionEntity("println", location,
                new PrimitiveTypeNode(location, "void"), parameters, null,
                FunctionEntity.EntityType.function);
        entities.put("println", function);

        // void printInt(int n);
        parameters = new ArrayList<>();
        parameters.add(VariableEntity.newEntity("n", "int"));
        function = new FunctionEntity("printInt", location,
                new PrimitiveTypeNode(location, "void"), parameters, null,
                FunctionEntity.EntityType.function);
        entities.put("printInt", function);

        // void printlnInt(int n);
        parameters = new ArrayList<>();
        parameters.add(VariableEntity.newEntity("n", "int"));
        function = new FunctionEntity("printlnInt", location,
                new PrimitiveTypeNode(location, "void"), parameters, null,
                FunctionEntity.EntityType.function);
        entities.put("printlnInt", function);

        // string getString();
        parameters = new ArrayList<>();
        function = new FunctionEntity("getString", location,
                new PrimitiveTypeNode(location, "string"), parameters, null,
                FunctionEntity.EntityType.function);
        entities.put("getString", function);

        // int getInt();
        parameters = new ArrayList<>();
        function = new FunctionEntity("getInt", location,
                new PrimitiveTypeNode(location, "int"), parameters, null,
                FunctionEntity.EntityType.function);
        entities.put("getInt", function);

        // string toString(int i);
        parameters = new ArrayList<>();
        parameters.add(VariableEntity.newEntity("i", "int"));
        function = new FunctionEntity("toString", location,
                new PrimitiveTypeNode(location, "string"), parameters, null,
                FunctionEntity.EntityType.function);
        entities.put("toString", function);
    }

    public void definedFunctionOrClass(String name, Location location, Scope globalScope,
                                          TypeTable typeTable, ErrorHandler errorHandler) throws CompilationError {
        if (globalScope.entities.containsKey(name)) {
            Entity globalEntity = globalScope.entities.get(name);
            if (globalEntity instanceof FunctionEntity) {
                errorHandler.error(location, "\"" + name + "\" is defined in global scope.");
                throw new CompilationError();
            }
        } else if (typeTable.hasType(new ClassTypeNode(location, name))) {
            errorHandler.error(location, "There is a class named \"" + name + "\".");
            throw new CompilationError();
        }
    }

    public void declareEntity(ProgramUnitNode unit, ErrorHandler errorHandler,
                              VariableEntity.EntityType varType, FunctionEntity.EntityType funcType,
                              Scope globalScope, TypeTable typeTable) throws CompilationError {
        Entity entity = null;
        // Rules:
        //    Name of global/local variables, parameters, members and methods
        //    can't be the same with name of functions and classes.
        if (unit instanceof VarNode) {
//            String name = ((VarNode) unit).getIdentifier();
//            definedFunctionOrClass(name, unit.getLocation(), globalScope, typeTable, errorHandler);
            // It may throw an error.

            entity = ((VarNode) unit).getEntity(varType);
        } else if (unit instanceof FunctionNode) {
//            String name = ((FunctionNode) unit).getIdentifier();
//            definedFunctionOrClass(name, unit.getLocation(), globalScope, typeTable, errorHandler);
            entity = ((FunctionNode) unit).getEntity(funcType);
        }

        assert entity != null;
        if (entities.containsKey(entity.getName())) {
            errorHandler.error(unit.getLocation(), "Duplicate declaration of \"" + entity.getName() + "\".");
            throw new CompilationError();
        } else
            entities.put(entity.getName(), entity);
    }

    public Entity getEntity(String name) {
        if (entities.containsKey(name))
            return entities.get(name);
        else if (parentScope != null)
            return parentScope.getEntity(name);
        else
            return null;
    }

    public boolean inClassScope() {
        if (scopeType == ScopeType.classScope)
            return true;
        else if (scopeType == ScopeType.programScope)
            return false;
        else
            return parentScope.inClassScope();
    }

    public boolean inFunctionScope() {
        if (scopeType == ScopeType.functionScope)
            return true;
        else if (scopeType == ScopeType.programScope)
            return false;
        else
            return parentScope.inFunctionScope();
    }

    public boolean inLoopScope() {
        if (scopeType == ScopeType.loopScope)
            return true;
        else if (scopeType == ScopeType.programScope)
            return false;
        else
            return parentScope.inLoopScope();
    }

    public boolean inMethodScope() {
        return inClassScope() && inFunctionScope();
    }
}
