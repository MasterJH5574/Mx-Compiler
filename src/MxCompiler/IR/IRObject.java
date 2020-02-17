package MxCompiler.IR;

import MxCompiler.IR.Instruction.IRInstruction;

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
}
