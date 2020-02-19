package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.Utilities.Pair;

import java.util.Set;

public class PhiInst extends IRInstruction {
    private Set<Pair<Operand, BasicBlock>> branch;
    private Operand result;

    public PhiInst(BasicBlock basicBlock, Set<Pair<Operand, BasicBlock>> branch, Register result) {
        super(basicBlock);
        this.branch = branch;
        this.result = result;

        for (Pair<Operand, BasicBlock> pair : branch) {
            assert pair.getFirst().getType().equals(result.getType())
                    || (pair.getFirst() instanceof ConstNull && result.getType() instanceof PointerType);
            pair.getFirst().addUse(this);
            pair.getSecond().addUse(this);
        }
        result.setDef(this);
    }

    public Set<Pair<Operand, BasicBlock>> getBranch() {
        return branch;
    }

    public void addBranch(Operand operand, BasicBlock block) {
        branch.add(new Pair<>(operand, block));
    }

    public void removeIncomingBlock(BasicBlock block) {
        Pair<Operand, BasicBlock> pair = null;
        for (Pair<Operand, BasicBlock> pairInBranch : branch) {
            if (pairInBranch.getSecond() == block) {
                pair = pairInBranch;
                break;
            }
        }
        assert pair != null;
        pair.getFirst().removeUse(this);
        pair.getSecond().removeUse(this);
        branch.remove(pair);
    }

    public Operand getResult() {
        return result;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        for (Pair<Operand, BasicBlock> pair : branch) {
            if (pair.getFirst() == oldUse)
                pair.setFirst((Operand) newUse);
            else if (pair.getSecond() == oldUse)
                pair.setSecond((BasicBlock) newUse);
        }
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(result.toString()).append(" = phi ").append(result.getType().toString()).append(" ");
        int size = branch.size();
        int cnt = 0;
        for (Pair<Operand, BasicBlock> pair : branch) {
            string.append("[ ").append(pair.getFirst().toString()).
                    append(", ").append(pair.getSecond().toString()).append(" ]");
            if (++cnt != size)
                string.append(", ");
        }
        return string.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
