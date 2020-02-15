package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IntegerType;

public class BranchInst extends IRInstruction {
    private Operand cond;
    private BasicBlock thenBlock;
    private BasicBlock elseBlock;

    public BranchInst(BasicBlock basicBlock, Operand cond, BasicBlock thenBlock, BasicBlock elseBlock) {
        super(basicBlock);
        this.cond = cond;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;

        basicBlock.getSuccessors().add(thenBlock);
        thenBlock.getPredecessors().add(basicBlock);

        if (cond != null) {
            basicBlock.getSuccessors().add(elseBlock);
            elseBlock.getPredecessors().add(basicBlock);
            assert cond.getType().equals(new IntegerType(IntegerType.BitWidth.int1));
        }
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
    public String toString() {
        if (cond != null)
            return "br i1 " + cond.toString() + ", label " + thenBlock.toString() + ", label " + elseBlock.toString();
        else
            return "br label " + thenBlock.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
