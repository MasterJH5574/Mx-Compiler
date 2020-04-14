package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;

abstract public class ASMInstruction {
    private BasicBlock basicBlock;
    private ASMInstruction prevInst;
    private ASMInstruction nextInst;

    public ASMInstruction(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
        prevInst = null;
        nextInst = null;
    }

    abstract public void accept(ASMVisitor visitor);
}