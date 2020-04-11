package MxCompiler.IR;

import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Instruction.ParallelCopyInst;
import MxCompiler.IR.Instruction.PhiInst;
import MxCompiler.IR.Instruction.ReturnInst;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.Utilities.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class BasicBlock extends IRObject implements Cloneable {
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
    private int reverseDfn;
    private BasicBlock reverseDfsFather;

    private BasicBlock idom;
    private BasicBlock semiDom;
    private ArrayList<BasicBlock> semiDomChildren;
    private HashSet<BasicBlock> strictDominators;

    private BasicBlock postIdom;
    private BasicBlock postSemiDom;
    private ArrayList<BasicBlock> postSemiDomChildren;
    private HashSet<BasicBlock> postStrictDominators;

    private HashSet<BasicBlock> DF; // Dominance Frontier
    private HashSet<BasicBlock> postDF;

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

    public void setFunction(Function function) {
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public String getNameWithoutDot() {
        if (name.contains(".")) {
            String[] strings = name.split("\\.");
            StringBuilder res = new StringBuilder();
            for (int i = 0; i < strings.length - 2; i++)
                res.append(strings[i]).append('.');
            res.append(strings[strings.length - 2]);
            return res.toString();
        } else
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

    public BasicBlock getPrev() {
        return prev;
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

    public void setPredecessors(Set<BasicBlock> predecessors) {
        this.predecessors = predecessors;
    }

    public Set<BasicBlock> getSuccessors() {
        return successors;
    }

    public void setSuccessors(Set<BasicBlock> successors) {
        this.successors = successors;
    }

    public void appendBlock(BasicBlock block) {
        block.prev = this;
        this.next = block;
    }

    public boolean isEmpty() {
        return instHead == instTail && instHead == null;
    }

    public boolean isNotExitBlock() {
        return !(instTail instanceof ReturnInst);
    }

    public void addInstruction(IRInstruction instruction) {
        boolean success;
        if (isEmpty()) {
            instHead = instruction;
            instTail = instruction;
            success = true;
        } else if (instTail.isNotTerminalInst()) {
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

    public void addInstructionPrev(IRInstruction inst1, IRInstruction inst2) {
        // Assure that inst1 is in this block.
        if (inst1.getInstPrev() == null) {
            inst1.setInstPrev(inst2);
            inst2.setInstNext(inst1);
            this.setInstHead(inst2);
        } else {
            inst2.setInstPrev(inst1.getInstPrev());
            inst2.setInstNext(inst1);
            inst1.getInstPrev().setInstNext(inst2);
            inst1.setInstPrev(inst2);
        }
        inst2.successfullyAdd();
    }

    public void addInstructionNext(IRInstruction inst1, IRInstruction inst2) {
        // Assure that inst1 is in this block.
        assert inst1.getInstNext() != null; // Since inst1 cannot be a BranchInst.
        inst2.setInstPrev(inst1);
        inst2.setInstNext(inst1.getInstNext());
        inst1.getInstNext().setInstPrev(inst2);
        inst1.setInstNext(inst2);
        inst2.successfullyAdd();
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

    public boolean notEndWithTerminalInst() {
        return instTail == null || instTail.isNotTerminalInst();
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

    public int getReverseDfn() {
        return reverseDfn;
    }

    public void setReverseDfn(int reverseDfn) {
        this.reverseDfn = reverseDfn;
    }

    public BasicBlock getReverseDfsFather() {
        return reverseDfsFather;
    }

    public void setReverseDfsFather(BasicBlock reverseDfsFather) {
        this.reverseDfsFather = reverseDfsFather;
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

    public BasicBlock getPostIdom() {
        return postIdom;
    }

    public void setPostIdom(BasicBlock postIdom) {
        this.postIdom = postIdom;
    }

    public BasicBlock getPostSemiDom() {
        return postSemiDom;
    }

    public void setPostSemiDom(BasicBlock postSemiDom) {
        this.postSemiDom = postSemiDom;
    }

    public ArrayList<BasicBlock> getPostSemiDomChildren() {
        return postSemiDomChildren;
    }

    public void setPostSemiDomChildren(ArrayList<BasicBlock> postSemiDomChildren) {
        this.postSemiDomChildren = postSemiDomChildren;
    }

    public HashSet<BasicBlock> getPostStrictDominators() {
        return postStrictDominators;
    }

    public void setPostStrictDominators(HashSet<BasicBlock> postStrictDominators) {
        this.postStrictDominators = postStrictDominators;
    }

    public HashSet<BasicBlock> getDF() {
        return DF;
    }

    public void setDF(HashSet<BasicBlock> DF) {
        this.DF = DF;
    }

    public HashSet<BasicBlock> getPostDF() {
        return postDF;
    }

    public void setPostDF(HashSet<BasicBlock> postDF) {
        this.postDF = postDF;
    }

    public ParallelCopyInst getParallelCopy() {
        IRInstruction ptr = this.getInstTail();
        while (ptr != null && !(ptr instanceof ParallelCopyInst))
            ptr = ptr.getInstPrev();
        return ptr == null ? null : ((ParallelCopyInst) ptr);
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

    public boolean dceRemoveFromFunction() {
        if (successors.size() != 1)
            return false;
        if (prev == null)
            function.setEntranceBlock(next);
        else
            prev.setNext(next);

        if (next == null)
            function.setExitBlock(prev);
        else
            next.setPrev(prev);

        BasicBlock successor = successors.iterator().next();
        this.replaceUse(successor);
        successor.getPredecessors().remove(this);
        for (BasicBlock predecessor : predecessors) {
            predecessor.getInstHead().replaceUse(this, successor);
            predecessor.getSuccessors().remove(this);
            predecessor.getSuccessors().add(successor);
            successor.getPredecessors().add(predecessor);
        }
        return true;
    }

    public void mergeBlock(BasicBlock block) {
        this.instTail.removeFromBlock();
        IRInstruction ptr = block.getInstHead();
        while (ptr != null) {
            if (ptr instanceof PhiInst) {
                IRInstruction next = ptr.getInstNext();
                assert ((PhiInst) ptr).getBranch().size() == 1;
                ptr.getResult().replaceUse(((PhiInst) ptr).getBranch().iterator().next().getFirst());
                ptr.removeFromBlock();
                ptr = next;
            } else {
                ptr.setBasicBlock(this);
                ptr.setInstPrev(this.instTail);
                if (this.isEmpty())
                    this.instHead = ptr;
                else
                    this.instTail.setInstNext(ptr);

                this.instTail = ptr;
                ptr = ptr.getInstNext();
            }
        }

        for (BasicBlock successor : block.getSuccessors()) {
            this.getSuccessors().add(successor);
            successor.getPredecessors().add(this);
        }
        block.setInstHead(null);
        block.setInstTail(null);
        block.replaceUse(this);
        block.removeFromFunction();
    }

    public BasicBlock split(IRInstruction instruction) {
        BasicBlock splitBlock = new BasicBlock(function, "inlineMergedBlock");
        function.getSymbolTable().put(splitBlock.getName(), splitBlock);

        splitBlock.setInstHead(instruction.getInstNext());
        splitBlock.setInstTail(this.instTail);
        this.setInstTail(instruction);

        instruction.getInstNext().setInstPrev(null);
        instruction.setInstNext(null);

        splitBlock.setNext(this.next);
        if (this.next != null)
            this.next.setPrev(splitBlock);
        splitBlock.setPrev(this);
        this.setNext(splitBlock);

        if (this.getFunction().getExitBlock() == this)
            this.getFunction().setExitBlock(splitBlock);

        for (BasicBlock successor : this.successors) {
            splitBlock.getSuccessors().add(successor);
            successor.getPredecessors().remove(this);
            successor.getPredecessors().add(splitBlock);

            IRInstruction ptr = successor.getInstHead();
            while (ptr instanceof PhiInst) {
                Operand operand = null;
                for (Pair<Operand, BasicBlock> pair : ((PhiInst) ptr).getBranch()) {
                    if (pair.getSecond() == this) {
                        operand = pair.getFirst();
                        break;
                    }
                }
                assert operand != null;
                ((PhiInst) ptr).removeIncomingBlock(this);
                ((PhiInst) ptr).addBranch(operand, splitBlock);
                ptr = ptr.getInstNext();
            }
        }
        this.successors = new LinkedHashSet<>();

        IRInstruction ptr = splitBlock.getInstHead();
        while (ptr != null) {
            ptr.setBasicBlock(splitBlock);
            ptr = ptr.getInstNext();
        }

        return splitBlock;
    }

    public boolean dominate(BasicBlock block) {
        return this == block || block.getStrictDominators().contains(this);
    }

    @Override
    public String toString() {
        return "%" + name;
    }

    @Override
    public Object clone() {
        BasicBlock block;
        try {
            block = ((BasicBlock) super.clone());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        ArrayList<IRInstruction> instructions = new ArrayList<>();
        IRInstruction ptr = this.instHead;
        while (ptr != null) {
            instructions.add((IRInstruction) ptr.clone());
            ptr = ptr.getInstNext();
        }
        for (int i = 0; i < instructions.size(); i++) {
            IRInstruction instruction = instructions.get(i);
            instruction.setInstPrev(i != 0 ? instructions.get(i - 1) : null);
            instruction.setInstNext(i != instructions.size() - 1 ? instructions.get(i + 1) : null);
            instruction.setBasicBlock(block);
        }

        block.function = this.function;
        block.name = this.name;
        if (instructions.isEmpty()) {
            block.instHead = null;
            block.instTail = null;
        } else {
            block.instHead = instructions.get(0);
            block.instTail = instructions.get(instructions.size() - 1);
        }
        block.prev = this.prev;
        block.next = this.next;
        block.predecessors = new HashSet<>(this.predecessors);
        block.successors = new HashSet<>(this.successors);
        return block;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
