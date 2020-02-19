package MxCompiler.IR;

import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Operand.Operand;

import java.util.LinkedHashSet;
import java.util.Set;


abstract public class IRObject {
    private Set<IRInstruction> use;

    public IRObject() {
        use = new LinkedHashSet<>();
    }

    public void addUse(IRInstruction instruction) {
        use.add(instruction);
    }

    public void removeUse(IRInstruction instruction) {
        assert use.contains(instruction);
        use.remove(instruction);
    }

    public Set<IRInstruction> getUse() {
        return use;
    }

    public void replaceUse(IRObject newUse) {
        assert (this instanceof Operand && newUse instanceof Operand)
                || (this instanceof BasicBlock && newUse instanceof BasicBlock)
                || (this instanceof Function && newUse instanceof Function);
        for (IRInstruction instruction : use)
            instruction.replaceUse(this, newUse);
        use.clear();
    }
}
