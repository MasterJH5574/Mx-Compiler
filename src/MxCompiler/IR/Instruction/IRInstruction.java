package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.SCCP;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

abstract public class IRInstruction implements Cloneable {
    private BasicBlock basicBlock;

    private IRInstruction instPrev;
    private IRInstruction instNext;

    public IRInstruction(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    public void setBasicBlock(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
    }

    abstract public void successfullyAdd();

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

    public boolean hasResult() {
        return this instanceof AllocateInst
                || this instanceof BinaryOpInst
                || this instanceof BitCastToInst
                || this instanceof CallInst
                || this instanceof GetElementPtrInst
                || this instanceof IcmpInst
                || this instanceof LoadInst
                || this instanceof PhiInst;
    }

    abstract public Register getResult();

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

    abstract public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue);

    abstract public boolean replaceResultWithConstant(SCCP sccp);

    public boolean canConvertToExpression() {
        assert !(this instanceof AllocateInst);
        return this instanceof BinaryOpInst
                || this instanceof BitCastToInst
                || this instanceof GetElementPtrInst
                || this instanceof IcmpInst;
    }

    abstract public CSE.Expression convertToExpression();

    abstract public void clonedUseReplace(Map<BasicBlock, BasicBlock> blockMap, Map<Operand, Operand> operandMap);

    @Override
    abstract public String toString();

    @Override
    public Object clone() {
        IRInstruction instruction;
        try {
            instruction = (IRInstruction) super.clone();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        instruction.basicBlock = this.basicBlock;
        instruction.instPrev = this.instPrev;
        instruction.instNext = this.instNext;
        return instruction;
    }

    abstract public void accept(IRVisitor visitor);
}
