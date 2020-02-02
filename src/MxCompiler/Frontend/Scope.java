package MxCompiler.Frontend;

import MxCompiler.AST.*;
import MxCompiler.Entity.Entity;
import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.Utilities.ErrorHandler;

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

    public Scope(Scope parentScope, ScopeType scopeType, TypeNode functionReturnType) {
        this.parentScope = parentScope;
        if (parentScope != null)
            parentScope.childrenScope.add(this);
        this.childrenScope = new ArrayList<>();
        this.entities = new HashMap<>();
        this.scopeType = scopeType;
        this.functionReturnType = functionReturnType;
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

    public void declareEntity(ProgramUnitNode unit, ErrorHandler errorHandler,
                              VariableEntity.EntityType varType, FunctionEntity.EntityType funcType) {
        Entity entity = null;
        if (unit instanceof VarNode)
            entity = ((VarNode) unit).getEntity(varType);
        else if (unit instanceof FunctionNode)
            entity = ((FunctionNode) unit).getEntity(funcType);

        assert entity != null;
        if (entities.containsKey(entity.getName()))
            errorHandler.error(unit.getLocation(), "Duplicate declaration of \"" + entity.getName() + "\".");
        else
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
