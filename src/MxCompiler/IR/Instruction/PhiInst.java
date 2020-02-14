package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.Utilities.Pair;

import java.util.ArrayList;

public class PhiInst extends IRInstruction {
    private ArrayList<Pair<Operand, BasicBlock>> branch;
    private Operand result;

    public PhiInst(BasicBlock basicBlock, ArrayList<Pair<Operand, BasicBlock>> branch, Operand result) {
        super(basicBlock);
        this.branch = branch;
        this.result = result;

        for (Pair<Operand, BasicBlock> pair : branch)
            assert pair.getFirst().getType().equals(result.getType());
    }

    public ArrayList<Pair<Operand, BasicBlock>> getBranch() {
        return branch;
    }

    public Operand getResult() {
        return result;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(result.toString()).append(" = phi ").append(result.getType().toString()).append(" ");
        for (int i = 0; i < branch.size(); i++) {
            string.append("[ ").append(branch.get(i).getFirst().toString()).
                    append(", ").append(branch.get(i).getSecond().toString()).append(" ]");
            if (i != branch.size() - 1)
                string.append(", ");
        }
        return string.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
