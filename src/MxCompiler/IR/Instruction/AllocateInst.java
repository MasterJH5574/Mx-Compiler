package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.Optim.Andersen;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.LoopOptim.LICM;
import MxCompiler.Optim.LoopOptim.LoopAnalysis;
import MxCompiler.Optim.SCCP;
import MxCompiler.Optim.SideEffectChecker;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AllocateInst extends IRInstruction {
    private Register result;
    private IRType type;

    public AllocateInst(BasicBlock basicBlock, Register result, IRType type) {
        // Note that "type" here should be converted to a pointer
        super(basicBlock);
        this.result = result;
        this.type = type;

        assert (new PointerType(type)).equals(result.getType());
    }

    @Override
    public void successfullyAdd() {
        result.setDef(this);
    }

    @Override
    public Register getResult() {
        return result;
    }

    public IRType getType() {
        return type;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        // do nothing.
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        // do nothing.
    }

    @Override
    public boolean replaceResultWithConstant(SCCP sccp) {
        SCCP.Status status = sccp.getStatus(result);
        if (status.getOperandStatus() == SCCP.Status.OperandStatus.constant) {
            result.replaceUse(status.getOperand());
            this.removeFromBlock();
            return true;
        } else
            return false;
    }

    @Override
    public CSE.Expression convertToExpression() {
        throw new RuntimeException("Convert alloca to expression.");
    }

    @Override
    public void clonedUseReplace(Map<BasicBlock, BasicBlock> blockMap, Map<Operand, Operand> operandMap) {
        // Do nothing.
    }

    @Override
    public void addConstraintsForAndersen(Map<Operand, Andersen.Node> nodeMap, Set<Andersen.Node> nodes) {
        assert nodeMap.containsKey(result);
        Andersen.Node pointer = nodeMap.get(result);
        Andersen.Node pointTo = new Andersen.Node(pointer.getName() + ".alloca");
        pointer.getPointsTo().add(pointTo);
        nodes.add(pointTo);
    }

    @Override
    public boolean updateResultScope(Map<Operand, SideEffectChecker.Scope> scopeMap,
                                     Map<Function, SideEffectChecker.Scope> returnValueScope) {
        if (scopeMap.get(result) != SideEffectChecker.Scope.local) {
            scopeMap.replace(result, SideEffectChecker.Scope.local);
            return true;
        } else
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
        return result.toString() + " = alloca " + type.toString();
    }

    @Override
    public Object clone() {
        AllocateInst allocateInst = (AllocateInst) super.clone();
        allocateInst.result = (Register) this.result.clone();
        allocateInst.type = this.type;

        allocateInst.result.setDef(allocateInst);
        return allocateInst;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
