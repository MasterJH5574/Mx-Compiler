package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;

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
}
