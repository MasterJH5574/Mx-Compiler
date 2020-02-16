package MxCompiler.IR;

import MxCompiler.IR.Operand.ConstString;
import MxCompiler.IR.Operand.GlobalVariable;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.TypeSystem.*;
import MxCompiler.Type.TypeTable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Module {
    private Map<String, Function> functionMap;
    private Map<String, GlobalVariable> globalVariableMap;
    private Map<String, StructureType> structureMap;
    private Map<String, GlobalVariable> constStringMap;
    private Map<String, Function> externalFunctionMap;

    private IRTypeTable irTypeTable;

    public Module(TypeTable astTypeTable) {
        functionMap = new LinkedHashMap<>();
        globalVariableMap = new LinkedHashMap<>();
        structureMap = new LinkedHashMap<>();
        constStringMap = new LinkedHashMap<>();
        irTypeTable = new IRTypeTable(this, astTypeTable);


        // Add external functions.
        externalFunctionMap = new LinkedHashMap<>();
        IRType returnType;
        ArrayList<Parameter> parameters;
        Function function;

        // void print(string str);
        returnType = new VoidType();
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str"));
        function = new Function(this, "print", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // void println(string str);
        returnType = new VoidType();
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str"));
        function = new Function(this, "println", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // void printInt(int n);
        returnType = new VoidType();
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new IntegerType(IntegerType.BitWidth.int32), "n"));
        function = new Function(this, "printInt", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // void printlnInt(int n);
        returnType = new VoidType();
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new IntegerType(IntegerType.BitWidth.int32), "n"));
        function = new Function(this, "printlnInt", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // string getString();
        returnType = new PointerType(new IntegerType(IntegerType.BitWidth.int8));
        parameters = new ArrayList<>();
        function = new Function(this, "getString", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // int getInt();
        returnType = new IntegerType(IntegerType.BitWidth.int32);
        parameters = new ArrayList<>();
        function = new Function(this, "getInt", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // string toString(int i);
        returnType = new PointerType(new IntegerType(IntegerType.BitWidth.int8));
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new IntegerType(IntegerType.BitWidth.int32), "i"));
        function = new Function(this, "toString", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // byte* malloc(int size);
        returnType = new PointerType(new IntegerType(IntegerType.BitWidth.int8));
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new IntegerType(IntegerType.BitWidth.int32), "size"));
        function = new Function(this, "malloc", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // string string.concatenate(string str1, string str2);
        returnType = new PointerType(new IntegerType(IntegerType.BitWidth.int8));
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str1"));
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str2"));
        function = new Function(this, "__string_concatenate", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // bool string.equal(string str1, string str2);
        returnType = new IntegerType(IntegerType.BitWidth.int1);
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str1"));
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str2"));
        function = new Function(this, "__string_equal", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // bool string.notEqual(string str1, string str2);
        returnType = new IntegerType(IntegerType.BitWidth.int1);
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str1"));
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str2"));
        function = new Function(this, "__string_notEqual", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // bool string.lessThan(string str1, string str2);
        returnType = new IntegerType(IntegerType.BitWidth.int1);
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str1"));
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str2"));
        function = new Function(this, "__string_lessThan", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // bool string.greaterThan(string str1, string str2);
        returnType = new IntegerType(IntegerType.BitWidth.int1);
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str1"));
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str2"));
        function = new Function(this, "__string_greaterThan", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // bool string.lessEqual(string str1, string str2);
        returnType = new IntegerType(IntegerType.BitWidth.int1);
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str1"));
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str2"));
        function = new Function(this, "__string_lessEqual", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // bool string.greaterEqual(string str1, string str2);
        returnType = new IntegerType(IntegerType.BitWidth.int1);
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str1"));
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str2"));
        function = new Function(this, "__string_greaterEqual", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // int string.length(string str);
        returnType = new IntegerType(IntegerType.BitWidth.int32);
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str"));
        function = new Function(this, "__string_length", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // string string.substring(string str, int left, int right);
        returnType = new PointerType(new IntegerType(IntegerType.BitWidth.int8));
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str"));
        parameters.add(new Parameter(new IntegerType(IntegerType.BitWidth.int32), "left"));
        parameters.add(new Parameter(new IntegerType(IntegerType.BitWidth.int32), "right"));
        function = new Function(this, "__string_substring", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // int string.parseInt(string str);
        returnType = new IntegerType(IntegerType.BitWidth.int32);
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str"));
        function = new Function(this, "__string_parseInt", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // int ord(string str, int pos);
        returnType = new IntegerType(IntegerType.BitWidth.int32);
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "str"));
        parameters.add(new Parameter(new IntegerType(IntegerType.BitWidth.int32), "pos"));
        function = new Function(this, "__string_ord", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);

        // int array.size(array arr);
        returnType = new IntegerType(IntegerType.BitWidth.int32);
        parameters = new ArrayList<>();
        parameters.add(new Parameter(new PointerType(new IntegerType(IntegerType.BitWidth.int8)), "arr"));
        function = new Function(this, "__array_size", returnType, parameters, true);
        externalFunctionMap.put(function.getName(), function);
    }

    public Map<String, Function> getFunctionMap() {
        return functionMap;
    }

    public Map<String, GlobalVariable> getGlobalVariableMap() {
        return globalVariableMap;
    }

    public Map<String, StructureType> getStructureMap() {
        return structureMap;
    }

    public Map<String, Function> getExternalFunctionMap() {
        return externalFunctionMap;
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

    public GlobalVariable addConstString(String string) {
        string = string.replace("\\\\", "\\");
        string = string.replace("\\n", "\n");
        string = string.replace("\\\"", "\"");
        string = string + "\0";
        if (constStringMap.containsKey(string))
            return constStringMap.get(string);
        else {
            int id = constStringMap.size();
            String name = ".str." + id;
            GlobalVariable globalVariable = new GlobalVariable(new ArrayType(string.length(),
                    new IntegerType(IntegerType.BitWidth.int8)), name, new ConstString(new ArrayType(string.length(),
                    new IntegerType(IntegerType.BitWidth.int8)), string));
            constStringMap.put(string, globalVariable);
            globalVariableMap.put(name, globalVariable);
            return globalVariable;
        }
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
