package MxCompiler.IR.Operand;

import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.TypeSystem.IRType;

import java.util.Queue;
import java.util.Set;

public class Register extends Operand implements Cloneable {
    private String name;
    private IRInstruction def;

    public Register(IRType type, String name) {
        super(type);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getFullName() {
        return def.getBasicBlock().getFunction().getName() + toString();
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

    public IRInstruction getDef() {
        return def;
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
        if (def.getBasicBlock().isNotExitBlock() && !live.contains(def.getBasicBlock().getInstTail())) {
            live.add(def.getBasicBlock().getInstTail());
            queue.offer(def.getBasicBlock().getInstTail());
        }
    }

    @Override
    public String toString() {
        return "%" + name;
    }

    @Override
    public Object clone() {
        Register register;
        try {
            register = (Register) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        register.name = this.name;
        register.def = this.def;
        return register;
    }
}
