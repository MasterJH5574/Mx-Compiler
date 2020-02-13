package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;

public class BranchInst extends IRInstruction {
    private Operand cond;
    private BasicBlock thenBlock;
    private BasicBlock elseBlock;

    public BranchInst(BasicBlock basicBlock, Operand cond, BasicBlock thenBlock, BasicBlock elseBlock) {
        super(basicBlock);
        this.cond = cond;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public Operand getCond() {
        return cond;
    }

    public BasicBlock getThenBlock() {
        return thenBlock;
    }

    public BasicBlock getElseBlock() {
        return elseBlock;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
