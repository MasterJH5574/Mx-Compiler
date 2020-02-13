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
    }

    public ArrayList<Pair<Operand, BasicBlock>> getBranch() {
        return branch;
    }

    public Operand getResult() {
        return result;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
