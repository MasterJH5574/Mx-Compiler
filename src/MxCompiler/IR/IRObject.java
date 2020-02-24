package MxCompiler.IR;

import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Operand.Operand;

import java.util.LinkedHashMap;
import java.util.Map;


abstract public class IRObject {
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
        for (IRInstruction instruction : use.keySet())
            instruction.replaceUse(this, newUse);
        use.clear();
    }
}
