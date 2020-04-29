package MxCompiler.RISCV;

import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class BasicBlock {
    private Function function;
    private String name;

    private ASMInstruction instHead;
    private ASMInstruction instTail;
    private BasicBlock prevBlock;
    private BasicBlock nextBlock;

    private Set<BasicBlock> predecessors;
    private Set<BasicBlock> successors;

    private Set<VirtualRegister> liveOut;
    private Set<VirtualRegister> UEVar;
    private Set<VirtualRegister> varKill;

    public BasicBlock(Function function, String name) {
        this.function = function;
        this.name = name;

        instHead = null;
        instTail = null;
        prevBlock = null;
        nextBlock = null;

        predecessors = new LinkedHashSet<>();
        successors = new LinkedHashSet<>();
    }

    public Function getFunction() {
        return function;
    }

    public boolean isEmpty() {
        return instHead == instTail && instHead == null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ASMInstruction getInstHead() {
        return instHead;
    }

    public ASMInstruction getInstTail() {
        return instTail;
    }

    public Set<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public Set<BasicBlock> getSuccessors() {
        return successors;
    }

    public Set<VirtualRegister> getLiveOut() {
        return liveOut;
    }

    public void setLiveOut(Set<VirtualRegister> liveOut) {
        this.liveOut = liveOut;
    }

    public Set<VirtualRegister> getUEVar() {
        return UEVar;
    }

    public void setUEVar(Set<VirtualRegister> UEVar) {
        this.UEVar = UEVar;
    }

    public Set<VirtualRegister> getVarKill() {
        return varKill;
    }

    public void setVarKill(Set<VirtualRegister> varKill) {
        this.varKill = varKill;
    }

    public void appendBlock(BasicBlock block) {
        block.prevBlock = this;
        this.nextBlock = block;
    }

    public void addInstruction(ASMInstruction instruction) {
        if (isEmpty())
            instHead = instruction;
        else {
            instTail.setNextInst(instruction);
            instruction.setPrevInst(instTail);
        }
        instTail = instruction;
    }

    public void addInstructionNext(ASMInstruction inst1, ASMInstruction inst2) {
        // It is ensured that inst1 is in this block.
        if (inst1 == instTail) {
            inst2.setPrevInst(inst1);
            inst2.setNextInst(null);
            inst1.setNextInst(inst2);
            instTail = inst2;
        } else {
            inst2.setPrevInst(inst1);
            inst2.setNextInst(inst1.getNextInst());
            inst1.getNextInst().setPrevInst(inst2);
            inst1.setNextInst(inst2);
        }
    }

    public void addInstructionPrev(ASMInstruction inst1, ASMInstruction inst2) {
        // It is ensured that inst1 is in this block.
        if (inst1 == instHead) {
            inst2.setNextInst(inst1);
            inst2.setPrevInst(null);
            inst1.setPrevInst(inst2);
            instHead = null;
        } else {
            inst2.setNextInst(inst1);
            inst2.setPrevInst(inst1.getPrevInst());
            inst1.getPrevInst().setNextInst(inst2);
            inst1.setPrevInst(inst2);
        }
    }

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
