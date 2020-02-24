package MxCompiler.IR.Operand;

import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.TypeSystem.IRType;

import java.util.Queue;
import java.util.Set;

abstract public class Operand extends IRObject {
    private IRType type;

    public Operand(IRType type) {
        this.type = type;
    }

    public IRType getType() {
        return type;
    }

    abstract public boolean isConstValue();

    abstract public void markAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue);

    @Override
    abstract public String toString();

    abstract public void accept(IRVisitor visitor);
}
