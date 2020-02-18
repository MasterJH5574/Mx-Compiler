package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;

abstract public class IRInstruction {
    private BasicBlock basicBlock;

    private IRInstruction instPrev;
    private IRInstruction instNext;

    public IRInstruction(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public void setInstPrev(IRInstruction instPrev) {
        this.instPrev = instPrev;
    }

    public void setInstNext(IRInstruction instNext) {
        this.instNext = instNext;
    }

    public IRInstruction getInstPrev() {
        return instPrev;
    }

    public IRInstruction getInstNext() {
        return instNext;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

    public boolean isTerminalInst() {
        return this instanceof BranchInst || this instanceof ReturnInst;
    }

    abstract public void replaceUse(IRObject oldUse, IRObject newUse);

    public void removeFromBlock() {
        if (instPrev == null)
            basicBlock.setInstHead(instNext);
        else
            instPrev.setInstNext(instNext);

        if (instNext == null)
            basicBlock.setInstTail(instPrev);
        else
            instNext.setInstPrev(instPrev);
    }

    @Override
    abstract public String toString();

    abstract public void accept(IRVisitor visitor);
}
