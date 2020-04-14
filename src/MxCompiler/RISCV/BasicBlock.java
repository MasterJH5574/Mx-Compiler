package MxCompiler.RISCV;

import MxCompiler.RISCV.Instruction.ASMInstruction;

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

    public void appendBlock(BasicBlock block) {
        block.prevBlock = this;
        this.nextBlock = block;
    }

    public void addInstruction(ASMInstruction instruction) {

    }

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
