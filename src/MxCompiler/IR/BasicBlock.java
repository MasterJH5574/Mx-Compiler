package MxCompiler.IR;

import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Instruction.PhiInst;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class BasicBlock extends IRObject {
    private Function function;

    private String name;

    private IRInstruction instHead;
    private IRInstruction instTail;

    private BasicBlock prev;
    private BasicBlock next;

    private Set<BasicBlock> predecessors;
    private Set<BasicBlock> successors;


    private int dfn;
    private BasicBlock dfsFather;

    private BasicBlock idom;
    private BasicBlock semiDom;
    private ArrayList<BasicBlock> semiDomChildren;
    private HashSet<BasicBlock> strictDominators;

    private HashSet<BasicBlock> DF; // Dominance Frontier

    public BasicBlock(Function function, String name) {
        this.function = function;
        this.name = name;

        instHead = null;
        instTail = null;
        prev = null;
        next = null;
        predecessors = new LinkedHashSet<>();
        successors = new LinkedHashSet<>();
    }

    public Function getFunction() {
        return function;
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

    public IRInstruction getInstHead() {
        return instHead;
    }

    public void setInstHead(IRInstruction instHead) {
        this.instHead = instHead;
    }

    public IRInstruction getInstTail() {
        return instTail;
    }

    public void setInstTail(IRInstruction instTail) {
        this.instTail = instTail;
    }

    public void setPrev(BasicBlock prev) {
        this.prev = prev;
    }

    public boolean hasNext() {
        return next != null;
    }

    public BasicBlock getNext() {
        return next;
    }

    public void setNext(BasicBlock next) {
        this.next = next;
    }

    public boolean hasPredecessor() {
        return predecessors.size() != 0;
    }

    public boolean hasSuccessor() {
        return successors.size() != 0;
    }

    public Set<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public Set<BasicBlock> getSuccessors() {
        return successors;
    }

    public void appendBlock(BasicBlock block) {
        block.prev = this;
        this.next = block;
    }

    private boolean isEmpty() {
        return instHead == instTail && instHead == null;
    }

    public void addInstruction(IRInstruction instruction) {
        boolean success;
        if (isEmpty()) {
            instHead = instruction;
            instTail = instruction;
            success = true;
        } else if (!instTail.isTerminalInst()) {
            instTail.setInstNext(instruction);
            instruction.setInstPrev(instTail);
            instTail = instruction;
            success = true;
        } else
            success = false;

        if (success)
            instruction.successfullyAdd();
    }

    public void addInstructionAtFront(IRInstruction instruction) {
        if (isEmpty())
            instTail = instruction;
        else {
            instHead.setInstPrev(instruction);
            instruction.setInstNext(instHead);
        }
        instHead = instruction;
        instruction.successfullyAdd();
    }

    public ArrayList<IRInstruction> getInstructions() {
        ArrayList<IRInstruction> instructions = new ArrayList<>();
        IRInstruction ptr = instHead;
        while (ptr != null) {
            instructions.add(ptr);
            ptr = ptr.getInstNext();
        }
        return instructions;
    }

    public boolean endWithTerminalInst() {
        return instTail.isTerminalInst();
    }

    public void removePhiIncomingBlock(BasicBlock block) {
        IRInstruction ptr = instHead;
        while (ptr instanceof PhiInst) {
            ((PhiInst) ptr).removeIncomingBlock(block);
            ptr = ptr.getInstNext();
        }
    }

    public int getDfn() {
        return dfn;
    }

    public void setDfn(int dfn) {
        this.dfn = dfn;
    }

    public BasicBlock getDfsFather() {
        return dfsFather;
    }

    public void setDfsFather(BasicBlock dfsFather) {
        this.dfsFather = dfsFather;
    }

    public BasicBlock getIdom() {
        return idom;
    }

    public void setIdom(BasicBlock idom) {
        this.idom = idom;
    }

    public BasicBlock getSemiDom() {
        return semiDom;
    }

    public void setSemiDom(BasicBlock semiDom) {
        this.semiDom = semiDom;
    }

    public ArrayList<BasicBlock> getSemiDomChildren() {
        return semiDomChildren;
    }

    public void setSemiDomChildren(ArrayList<BasicBlock> semiDomChildren) {
        this.semiDomChildren = semiDomChildren;
    }

    public HashSet<BasicBlock> getStrictDominators() {
        return strictDominators;
    }

    public void setStrictDominators(HashSet<BasicBlock> strictDominators) {
        this.strictDominators = strictDominators;
    }

    public HashSet<BasicBlock> getDF() {
        return DF;
    }

    public void setDF(HashSet<BasicBlock> DF) {
        this.DF = DF;
    }

    public void removeFromFunction() {
        for (IRInstruction instruction : getInstructions())
            instruction.removeFromBlock();

        if (prev == null) {
            function.setEntranceBlock(next);
            throw new RuntimeException();
        } else
            prev.setNext(next);

        if (next == null)
            function.setExitBlock(prev);
        else
            next.setPrev(prev);

        for (BasicBlock predecessor : predecessors)
            predecessor.getSuccessors().remove(this);
        for (BasicBlock successor : successors)
            successor.getPredecessors().remove(this);
    }

    public void mergeBlock(BasicBlock block) {
        this.instTail.removeFromBlock();
        IRInstruction ptr = block.getInstHead();
        while (ptr != null) {
            ptr.setBasicBlock(this);
            ptr.setInstPrev(this.instTail);
            if (this.isEmpty())
                this.instHead = ptr;
            else
                this.instTail.setInstNext(ptr);

            this.instTail = ptr;
            ptr = ptr.getInstNext();
        }

        for (BasicBlock successor : block.getSuccessors()) {
            this.getSuccessors().add(successor);
            successor.getPredecessors().add(this);
        }
        block.setInstHead(null);
        block.setInstTail(null);
        block.removeFromFunction();
    }

    @Override
    public String toString() {
        return "%" + name;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
