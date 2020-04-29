package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.*;

abstract public class ASMInstruction {
    private BasicBlock basicBlock;
    private ASMInstruction prevInst;
    private ASMInstruction nextInst;

    private Map<VirtualRegister, Integer> def;
    private Map<VirtualRegister, Integer> use;

    public ASMInstruction(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
        prevInst = null;
        nextInst = null;

        def = new LinkedHashMap<>();
        use = new LinkedHashMap<>();
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public ASMInstruction getPrevInst() {
        return prevInst;
    }

    public void setPrevInst(ASMInstruction prevInst) {
        this.prevInst = prevInst;
    }

    public ASMInstruction getNextInst() {
        return nextInst;
    }

    public void setNextInst(ASMInstruction nextInst) {
        this.nextInst = nextInst;
    }

    public void addDef(VirtualRegister vr) {
        if (def.containsKey(vr))
            def.replace(vr, def.get(vr) + 1);
        else
            def.put(vr, 1);
    }

    public void removeDef(VirtualRegister vr) {
        assert def.containsKey(vr);
        if (def.get(vr) == 1)
            def.remove(vr);
        else
            def.replace(vr, def.get(vr) - 1);
    }

    public void replaceDef(VirtualRegister oldVR, VirtualRegister newVR) {
        Queue<VirtualRegister> addList = new LinkedList<>();
        Queue<VirtualRegister> removeList = new LinkedList<>();
        for (VirtualRegister def : this.def.keySet()) {
            if (def == oldVR) {
                addList.add(newVR);
                removeList.add(oldVR);
            }
        }
        for (VirtualRegister vr : removeList)
            this.removeDef(vr);
        for (VirtualRegister vr : addList)
            this.addDef(vr);
    }

    public Set<VirtualRegister> getDef() {
        return def.keySet();
    }

    public void addUse(VirtualRegister vr) {
        if (use.containsKey(vr))
            use.replace(vr, use.get(vr) + 1);
        else
            use.put(vr, 1);
    }

    public void removeUse(VirtualRegister vr) {
        assert use.containsKey(vr);
        if (use.get(vr) == 1)
            use.remove(vr);
        else
            use.replace(vr, use.get(vr) - 1);
    }

    public void replaceUse(VirtualRegister oldVR, VirtualRegister newVR) {
        Queue<VirtualRegister> addList = new LinkedList<>();
        Queue<VirtualRegister> removeList = new LinkedList<>();
        for (VirtualRegister use : this.use.keySet()) {
            if (use == oldVR) {
                addList.add(newVR);
                removeList.add(oldVR);
            }
        }
        for (VirtualRegister vr : removeList)
            this.removeUse(vr);
        for (VirtualRegister vr : addList)
            this.addUse(vr);
    }

    public Set<VirtualRegister> getUse() {
        return use.keySet();
    }

    public Set<VirtualRegister> getDefUseUnion() {
        Set<VirtualRegister> union = new HashSet<>(getDef());
        union.addAll(getUse());
        return union;
    }

    public void addToUEVarAndVarKill(Set<VirtualRegister> UEVar, Set<VirtualRegister> varKill) {

    }

    abstract public void accept(ASMVisitor visitor);
}
