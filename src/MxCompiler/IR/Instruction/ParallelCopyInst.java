package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.Optim.Andersen;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.LoopOptim.LICM;
import MxCompiler.Optim.LoopOptim.LoopAnalysis;
import MxCompiler.Optim.SCCP;
import MxCompiler.Optim.SideEffectChecker;

import java.util.*;

public class ParallelCopyInst extends IRInstruction {
    private Set<MoveInst> moves;

    public ParallelCopyInst(BasicBlock basicBlock) {
        super(basicBlock);
        moves = new HashSet<>();
    }

    public void appendMove(MoveInst moveInst) {
        if (moveInst.getResult().equals(moveInst.getSource()))
            return;
        moves.add(moveInst);
    }

    public void removeMove(MoveInst moveInst) {
        assert moves.contains(moveInst);
        moves.remove(moveInst);
    }

    public MoveInst getMove() {
        if (moves.isEmpty())
            return null;
        return moves.iterator().next();
    }

    public MoveInst findValidMove() {
        for (MoveInst move1 : moves) {
            boolean flag = true;
            for (MoveInst move2 : moves) {
                if (move2.getSource().equals(move1.getResult())) {
                    flag = false;
                    break;
                }
            }
            if (flag)
                return move1;
        }
        return null;
    }

    public Set<MoveInst> getMoves() {
        return moves;
    }

    @Override
    public void successfullyAdd() {
        // Do nothing.
    }

    @Override
    public Register getResult() {
        return null;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        // Do nothing.
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        // Do nothing.
    }

    @Override
    public boolean replaceResultWithConstant(SCCP sccp) {
        return false;
    }

    @Override
    public CSE.Expression convertToExpression() {
        return null;
    }

    @Override
    public void clonedUseReplace(Map<BasicBlock, BasicBlock> blockMap, Map<Operand, Operand> operandMap) {
        // Do nothing.
    }

    @Override
    public void addConstraintsForAndersen(Map<Operand, Andersen.Node> nodeMap, Set<Andersen.Node> nodes) {
        // Do nothing.
    }

    @Override
    public boolean updateResultScope(Map<Operand, SideEffectChecker.Scope> scopeMap,
                                     Map<Function, SideEffectChecker.Scope> returnValueScope) {
        return false;
    }

    @Override
    public boolean checkLoopInvariant(LoopAnalysis.LoopNode loop, LICM licm) {
        return false;
    }

    @Override
    public boolean canBeHoisted(LoopAnalysis.LoopNode loop) {
        return false;
    }

    @Override
    public boolean combineInst(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("parallelCopy ");
        Iterator<MoveInst> it = moves.iterator();
        while (it.hasNext()) {
            MoveInst move = it.next();
            string.append("[ ").append(move.getResult()).append(", ").append(move.getSource()).append(" ]");
            if (it.hasNext())
                string.append(", ");
        }
        return string.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        // Do nothing.
    }
}
