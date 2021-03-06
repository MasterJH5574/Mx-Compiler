package MxCompiler.RISCV;

import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Instruction.JumpInst;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.LinkedHashSet;
import java.util.Set;

public class BasicBlock {
    private Function function;
    private String name;
    private String asmName;

    private MxCompiler.IR.BasicBlock irBlock;

    private ASMInstruction instHead;
    private ASMInstruction instTail;
    private BasicBlock prevBlock;
    private BasicBlock nextBlock;

    private Set<BasicBlock> predecessors;
    private Set<BasicBlock> successors;

    private Set<VirtualRegister> liveOut;
    private Set<VirtualRegister> UEVar;
    private Set<VirtualRegister> varKill;

    public BasicBlock(Function function, MxCompiler.IR.BasicBlock irBlock, String name, String asmName) {
        this.function = function;
        this.name = name;
        this.asmName = asmName;

        this.irBlock = irBlock;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAsmName() {
        return asmName;
    }

    public MxCompiler.IR.BasicBlock getIrBlock() {
        return irBlock;
    }

    public boolean isEmpty() {
        return instHead == instTail && instHead == null;
    }

    public ASMInstruction getInstHead() {
        return instHead;
    }

    public void setInstHead(ASMInstruction instHead) {
        this.instHead = instHead;
    }

    public ASMInstruction getInstTail() {
        return instTail;
    }

    public void setInstTail(ASMInstruction instTail) {
        this.instTail = instTail;
    }

    public void setPrevBlock(BasicBlock prevBlock) {
        this.prevBlock = prevBlock;
    }

    public BasicBlock getPrevBlock() {
        return prevBlock;
    }

    public void setNextBlock(BasicBlock nextBlock) {
        this.nextBlock = nextBlock;
    }

    public BasicBlock getNextBlock() {
        return nextBlock;
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

    public void addInstructionAtFront(ASMInstruction instruction) {
        if (isEmpty())
            instTail = instruction;
        else {
            instHead.setPrevInst(instruction);
            instruction.setNextInst(instHead);
        }
        instHead = instruction;
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
            instHead = inst2;
        } else {
            inst2.setNextInst(inst1);
            inst2.setPrevInst(inst1.getPrevInst());
            inst1.getPrevInst().setNextInst(inst2);
            inst1.setPrevInst(inst2);
        }
    }

    public void removeTailJump() {
        assert instTail instanceof JumpInst;
        JumpInst jump = ((JumpInst) instTail);
        if (jump.getPrevInst() == null) {
            instHead = null;
            instTail = null;
        } else {
            jump.getPrevInst().setNextInst(null);
            instTail = jump.getPrevInst();
        }
        jump.setDest(null);
    }

    public String emitCode() {
        return asmName;
    }

    @Override
    public String toString() {
        return name;
    }

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
