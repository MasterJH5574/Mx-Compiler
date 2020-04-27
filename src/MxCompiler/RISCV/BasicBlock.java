package MxCompiler.RISCV;

import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.Register.Register;

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

    private Set<Register> liveOut;
    private Set<Register> UEVar;
    private Set<Register> varKill;

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

    public Set<Register> getLiveOut() {
        return liveOut;
    }

    public void setLiveOut(Set<Register> liveOut) {
        this.liveOut = liveOut;
    }

    public Set<Register> getUEVar() {
        return UEVar;
    }

    public void setUEVar(Set<Register> UEVar) {
        this.UEVar = UEVar;
    }

    public Set<Register> getVarKill() {
        return varKill;
    }

    public void setVarKill(Set<Register> varKill) {
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

    public void addInstructionAtFront(ASMInstruction instruction) {
        if (isEmpty())
            instTail = instruction;
        else {
            instHead.setPrevInst(instruction);
            instruction.setNextInst(instHead);
        }
        instHead = instruction;
    }

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
