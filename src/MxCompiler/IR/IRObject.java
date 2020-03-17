package MxCompiler.IR;

import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Operand.Operand;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


abstract public class IRObject implements Cloneable {
    private Map<IRInstruction, Integer> use;

    public IRObject() {
        use = new LinkedHashMap<>();
    }

    public void addUse(IRInstruction instruction) {
        if (!use.containsKey(instruction))
            use.put(instruction, 1);
        else
            use.replace(instruction, use.get(instruction) + 1);
    }

    public void removeUse(IRInstruction instruction) {
        int cnt = use.get(instruction);
        assert use.containsKey(instruction) && cnt > 0;
        if (cnt == 1)
            use.remove(instruction);
        else
            use.replace(instruction, cnt - 1);
    }

    public Map<IRInstruction, Integer> getUse() {
        return use;
    }

    public void replaceUse(IRObject newUse) {
        assert (this instanceof Operand && newUse instanceof Operand)
                || (this instanceof BasicBlock && newUse instanceof BasicBlock)
                || (this instanceof Function && newUse instanceof Function);
        ArrayList<IRInstruction> instructions = new ArrayList<>(use.keySet());
        for (IRInstruction instruction : instructions)
            instruction.replaceUse(this, newUse);
        use.clear();
    }

    @Override
    public Object clone() {
        IRObject irObject;
        try {
            irObject = (IRObject) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        irObject.use = new LinkedHashMap<>();
        return irObject;
    }
}
