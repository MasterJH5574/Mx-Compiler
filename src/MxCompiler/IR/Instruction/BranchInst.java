package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IntegerType;

import javax.management.relation.RoleList;

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

            cond.addUse(this);
            elseBlock.addUse(this);
        }
        thenBlock.addUse(this);
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
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (cond == oldUse)
            cond = (Operand) newUse;
        else {
            if (thenBlock == oldUse)
                thenBlock = (BasicBlock) newUse;
            if (elseBlock == oldUse)
                elseBlock = (BasicBlock) newUse;
        }
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
