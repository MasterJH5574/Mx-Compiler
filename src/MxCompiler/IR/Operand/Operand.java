package MxCompiler.IR.Operand;

import MxCompiler.IR.IRObject;
import MxCompiler.IR.Instruction.BinaryOpInst;
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

    abstract public void markBaseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue);

    public boolean registerDefIsBinaryOpInst() {
        return this instanceof Register && ((Register) this).getDef() instanceof BinaryOpInst;
    }

    public int getPrivilege() {
        if (this instanceof Parameter)
            return 3;
        if (this instanceof Constant)
            return 0;
        assert this instanceof Register;
        if (!(((Register) this).getDef() instanceof BinaryOpInst))
            return 3;

        BinaryOpInst def = ((BinaryOpInst) ((Register) this).getDef());
        if (def.isIntegerNot() || def.isNegative())
            return 1;
        else
            return 2;
    }

    @Override
    abstract public String toString();

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Operand))
            return false;
        return this.toString().equals(obj.toString());
    }
}
