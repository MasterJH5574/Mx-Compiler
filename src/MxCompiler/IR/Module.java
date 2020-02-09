package MxCompiler.IR;

import MxCompiler.IR.Operand.GlobalVariable;
import MxCompiler.IR.TypeSystem.IRTypeTable;
import MxCompiler.IR.TypeSystem.StructureType;
import MxCompiler.Type.TypeTable;

import java.util.LinkedHashMap;
import java.util.Map;

public class Module {
    private Map<String, Function> functionMap;
    private Map<String, GlobalVariable> globalVariableMap;
    private Map<String, StructureType> structureMap;
    private Map<String, GlobalVariable> constStringMap;

    private IRTypeTable irTypeTable;

    public Module(TypeTable astTypeTable) {
        functionMap = new LinkedHashMap<>();
        globalVariableMap = new LinkedHashMap<>();
        structureMap = new LinkedHashMap<>();
        constStringMap = new LinkedHashMap<>();
        irTypeTable = new IRTypeTable(this, astTypeTable);
    }

    public IRTypeTable getIrTypeTable() {
        return irTypeTable;
    }

    public void addFunction(Function function) {
        functionMap.put(function.getName(), function);
    }

    public void addGlobalVariable(GlobalVariable globalVariable) {
        globalVariableMap.put(globalVariable.getName(), globalVariable);
    }

    public void addStructure(StructureType structure) {
        structureMap.put(structure.getName(), structure);
    }
}
