package MxCompiler.Utilities;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, ArrayList<Object>> symbolTable;

    public SymbolTable() {
        symbolTable = new HashMap<>();
    }

    public void put(String name, Object object) {
        ArrayList<Object> arrayList;
        int label;
        if (symbolTable.containsKey(name)) {
            arrayList = symbolTable.get(name);
            label = arrayList.size();
        } else {
            arrayList = new ArrayList<>();
            symbolTable.put(name, arrayList);
            label = 0;
        }

        if (object instanceof BasicBlock)
            ((BasicBlock) object).setName(name + "." + label);
        else if (object instanceof Parameter)
            ((Parameter) object).setName(name + "." + label);
        else if (object instanceof Register)
            ((Register) object).setName(name + "." + label);
        else {
            // Todo
            throw new RuntimeException();
        }
        arrayList.add(object);
    }

    public Object get(String name) {
        // This method may be called only by "visit(ThisExprNode node)".
        ArrayList<Object> arrayList = symbolTable.get(name);
        return arrayList.get(0);
    }
}
