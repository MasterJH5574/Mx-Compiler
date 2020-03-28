package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.Optim.Andersen;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.LoopOptim.LICM;
import MxCompiler.Optim.LoopOptim.LoopAnalysis;
import MxCompiler.Optim.SCCP;
import MxCompiler.Optim.SideEffectChecker;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BitCastToInst extends IRInstruction {
    private Operand src;
    private IRType objectType;
    private Register result;

    public BitCastToInst(BasicBlock basicBlock, Operand src, IRType objectType, Register result) {
        super(basicBlock);
        this.src = src;
        this.objectType = objectType;
        this.result = result;

        assert result.getType().equals(objectType);
        assert objectType instanceof PointerType;
        assert src.getType() instanceof PointerType;
    }

    @Override
    public void successfullyAdd() {
        result.setDef(this);
        src.addUse(this);
    }

    public Operand getSrc() {
        return src;
    }

    public IRType getObjectType() {
        return objectType;
    }

    @Override
    public Register getResult() {
        return result;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (src == oldUse) {
            src.removeUse(this);
            src = (Operand) newUse;
            src.addUse(this);
        }
    }

    @Override
    public void removeFromBlock() {
        src.removeUse(this);
        super.removeFromBlock();
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        src.markBaseAsLive(live, queue);
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
        String instructionName = "bitcast";
        ArrayList<String> operands = new ArrayList<>();
        operands.add(src.toString());
        operands.add(objectType.toString());
        return new CSE.Expression(instructionName, operands);
    }

    @Override
    public void clonedUseReplace(Map<BasicBlock, BasicBlock> blockMap, Map<Operand, Operand> operandMap) {
        if (src instanceof Parameter || src instanceof Register) {
            assert operandMap.containsKey(src);
            src = operandMap.get(src);
        }
        src.addUse(this);
    }

    @Override
    public void addConstraintsForAndersen(Map<Operand, Andersen.Node> nodeMap, Set<Andersen.Node> nodes) {
        assert src.getType() instanceof PointerType && result.getType() instanceof PointerType;
        if (!(src instanceof ConstNull)) {
            assert nodeMap.containsKey(result);
            assert nodeMap.containsKey(src);
            nodeMap.get(src).getInclusiveEdge().add(nodeMap.get(result));
        }
    }

    @Override
    public boolean updateResultScope(Map<Operand, SideEffectChecker.Scope> scopeMap,
                                     Map<Function, SideEffectChecker.Scope> returnValueScope) {
        if (src instanceof ConstNull) {
            if (scopeMap.get(result) != SideEffectChecker.Scope.local) {
                scopeMap.replace(result, SideEffectChecker.Scope.local);
                return true;
            } else
                return false;
        }
        SideEffectChecker.Scope scope = scopeMap.get(src);
        assert scope != SideEffectChecker.Scope.undefined;
        if (scopeMap.get(result) != scope) {
            scopeMap.replace(result, scope);
            return true;
        } else
            return false;
    }

    @Override
    public boolean checkLoopInvariant(LoopAnalysis.LoopNode loop, LICM licm) {
        if (licm.isLoopInvariant(result, loop))
            return false;

        if (licm.isLoopInvariant(src, loop)) {
            licm.markLoopInvariant(result);
            return true;
        }
        return false;
    }

    @Override
    public boolean canBeHoisted(LoopAnalysis.LoopNode loop) {
        return loop.defOutOfLoop(src);
    }

    @Override
    public boolean combineInst(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        if (src.getType().equals(objectType)) {
            for (IRInstruction instruction : result.getUse().keySet()) {
                if (!inQueue.contains(instruction)) {
                    queue.offer(instruction);
                    inQueue.add(instruction);
                }
            }
            result.replaceUse(src);
            return true;
        } else
            return false;
    }

    @Override
    public String toString() {
        return result.toString() + " = bitcast "
                + src.getType().toString() + " " + src.toString() + " to " + objectType.toString();
    }

    @Override
    public Object clone() {
        BitCastToInst bitCastToInst = ((BitCastToInst) super.clone());
        bitCastToInst.src = this.src;
        bitCastToInst.objectType = this.objectType;
        bitCastToInst.result = (Register) this.result.clone();

        bitCastToInst.result.setDef(bitCastToInst);
        return bitCastToInst;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
