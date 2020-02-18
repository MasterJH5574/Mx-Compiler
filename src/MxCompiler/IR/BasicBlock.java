package MxCompiler.IR;

import MxCompiler.IR.Instruction.IRInstruction;

import java.util.ArrayList;
import java.util.HashSet;

public class BasicBlock extends IRObject {
    private Function function;

    private String name;

    private IRInstruction instHead;
    private IRInstruction instTail;

    private BasicBlock prev;
    private BasicBlock next;

    private ArrayList<BasicBlock> predecessors;
    private ArrayList<BasicBlock> successors;


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
        predecessors = new ArrayList<>();
        successors = new ArrayList<>();
    }

    public String getName() {
        return name;
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

    public boolean hasNext() {
        return next != null;
    }

    public BasicBlock getNext() {
        return next;
    }

    public boolean hasPredecessor() {
        return predecessors.size() != 0;
    }

    public boolean hasSuccessor() {
        return successors.size() != 0;
    }

    public ArrayList<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public ArrayList<BasicBlock> getSuccessors() {
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
        if (isEmpty()) {
            instHead = instruction;
            instTail = instruction;
        } else if (!instTail.isTerminalInst()) {
            instTail.setInstNext(instruction);
            instruction.setInstPrev(instTail);
            instTail = instruction;
        }
        // else do nothing
    }

    public void addInstructionAtFront(IRInstruction instruction) {
        if (isEmpty())
            instTail = instruction;
        else {
            instHead.setInstPrev(instruction);
            instruction.setInstNext(instHead);
        }
        instHead = instruction;
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

    @Override
    public String toString() {
        return "%" + name;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
