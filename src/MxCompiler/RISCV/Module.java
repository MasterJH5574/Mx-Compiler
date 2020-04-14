package MxCompiler.RISCV;

import MxCompiler.RISCV.Operand.GlobalVariable;

import java.util.LinkedHashMap;
import java.util.Map;

public class Module {
    private Map<String, Function> functionMap;
    private Map<String, Function> externalFunctionMap;
    private Map<String, GlobalVariable> globalVariableMap;

    public Module() {
        functionMap = new LinkedHashMap<>();
        externalFunctionMap = new LinkedHashMap<>();
        globalVariableMap = new LinkedHashMap<>();
    }

    public Map<String, Function> getFunctionMap() {
        return functionMap;
    }

    public Map<String, Function> getExternalFunctionMap() {
        return externalFunctionMap;
    }

    public Map<String, GlobalVariable> getGlobalVariableMap() {
        return globalVariableMap;
    }

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
