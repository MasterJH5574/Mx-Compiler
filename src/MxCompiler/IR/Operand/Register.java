package MxCompiler.IR.Operand;

import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.TypeSystem.IRType;

import java.util.Queue;
import java.util.Set;

public class Register extends Operand {
    private String name;
    private IRInstruction def;

    public Register(IRType type, String name) {
        super(type);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getNameWithoutDot() {
        if (name.contains("."))
            return name.split("\\.")[0];
        else
            throw new RuntimeException();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDef(IRInstruction def) {
        this.def = def;
    }

    @Override
    public boolean isConstValue() {
        return false;
    }

    @Override
    public void markBaseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        assert def != null;
        if (!live.contains(def)) {
            live.add(def);
            queue.offer(def);
        }
    }

    @Override
    public String toString() {
        return "%" + name;
    }
}
