package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Constant;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;
import MxCompiler.Optim.Andersen;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.LoopOptim.LICM;
import MxCompiler.Optim.LoopOptim.LoopAnalysis;
import MxCompiler.Optim.SCCP;
import MxCompiler.Optim.SideEffectChecker;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class MoveInst extends IRInstruction {
    private Operand source;
    private Register result;

    public MoveInst(BasicBlock basicBlock, Operand source, Register result) {
        super(basicBlock);
        this.source = source;
        this.result = result;

        assert source.getType().equals(result.getType());
        assert source instanceof Register || source instanceof Parameter || source instanceof Constant;
    }

    @Override
    public void successfullyAdd() {
        result.setDef(this);
        source.addUse(this);
    }

    @Override
    public Register getResult() {
        return result;
    }

    public Operand getSource() {
        return source;
    }

    public void setSource(Operand source) {
        this.source = source;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (source == oldUse) {
            source.removeUse(this);
            source = (Operand) newUse;
            source.addUse(this);
        }
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
    public boolean updateResultScope(Map<Operand, SideEffectChecker.Scope> scopeMap, Map<Function, SideEffectChecker.Scope> returnValueScope) {
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
        return "move " + result.toString() + " " + source.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
