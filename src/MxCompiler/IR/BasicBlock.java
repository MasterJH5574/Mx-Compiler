package MxCompiler.IR;

import MxCompiler.IR.Instruction.IRInstruction;

import java.util.ArrayList;

public class BasicBlock {
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

    public void appendBlock(BasicBlock block) {
        block.prev = this;
        this.next = block;
    }

    private boolean isEmpty() {
        return instHead == instTail;
    }

    public void addInstruction(IRInstruction instruction) {
        if (isEmpty()) {
            instHead = instruction;
        } else {
            instTail.setInstNext(instruction);
            instruction.setInstNext(instTail);
        }
        instTail = instruction;
        // Question: cause problem?
        // Todo
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
}
