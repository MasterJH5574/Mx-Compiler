package MxCompiler.IR.Operand;

import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.TypeSystem.IRType;

import java.util.Queue;
import java.util.Set;

abstract public class Constant extends Operand {
    public Constant(IRType type) {
        super(type);
    }

    @Override
    public boolean isConstValue() {
        return true;
    }

    @Override
    public void markAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        // do nothing.
    }
}
