package MxCompiler.IR;

import MxCompiler.IR.Instruction.IRInstruction;

import java.util.ArrayList;

public class BasicBlock extends IRObject {
    private Function function;

    private String name;

    private IRInstruction instHead;
    private IRInstruction instTail;

    private BasicBlock prev;
    private BasicBlock next;

    private ArrayList<BasicBlock> predecessors;
    private ArrayList<BasicBlock> successors;

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

    public IRInstruction getInstTail() {
        return instTail;
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

    public boolean endWithTerminalInst() {
        return instTail.isTerminalInst();
    }

    @Override
    public String toString() {
        return "%" + name;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
