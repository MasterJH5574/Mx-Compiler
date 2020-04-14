package MxCompiler.RISCV;

import MxCompiler.RISCV.Operand.GlobalVariable;

import java.util.LinkedHashMap;
import java.util.Map;

public class Module {
    private Map<String, Function> functionMap;
    private Map<String, GlobalVariable> globalVariableMap;
    private Map<String, Function> externalFunctionMap;

    public Module() {
        functionMap = new LinkedHashMap<>();
        globalVariableMap = new LinkedHashMap<>();
        externalFunctionMap = new LinkedHashMap<>();
    }

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
