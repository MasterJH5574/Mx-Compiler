package MxCompiler.Utilities;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.*;

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
        else
            throw new RuntimeException();
        arrayList.add(object);
    }

    public void putASM(String name, Object object) {
        assert object instanceof VirtualRegister || object instanceof MxCompiler.RISCV.BasicBlock;
        assert !symbolTable.containsKey(name);

        ArrayList<Object> array = new ArrayList<>();
        array.add(object);
        symbolTable.put(name, array);
    }

    public void putASMRename(String name, Object object) {
        // Maybe this method will only be called when loading/storing global variables.
        assert object instanceof VirtualRegister;

        int id = 0;
        while (symbolTable.containsKey(name + "." + id))
            id++;
        ((VirtualRegister) object).setName(name + "." + id);

        ArrayList<Object> array = new ArrayList<>();
        array.add(object);
        symbolTable.put(name, array);
    }

    public boolean contains(String name) {
        return symbolTable.containsKey(name);
    }

    public Object get(String name) {
        ArrayList<Object> arrayList = symbolTable.get(name);
        return arrayList.get(0);
    }

    public VirtualRegister getVR(String name) {
        assert symbolTable.containsKey(name);
        return ((VirtualRegister) symbolTable.get(name).get(0));
    }

    public Set<VirtualRegister> getAllVRs() {
        Set<VirtualRegister> VRs = new HashSet<>();
        for (ArrayList<Object> array : symbolTable.values()) {
            assert array.size() == 1;
            assert array.get(0) instanceof VirtualRegister;
            VRs.add(((VirtualRegister) array.get(0)));
        }
        return VRs;
    }

    public void removeVR(VirtualRegister vr) {
        assert symbolTable.containsKey(vr.getName());
        assert symbolTable.get(vr.getName()).get(0) == vr;
        symbolTable.remove(vr.getName());
    }
}
