package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IntegerType;
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

public class IcmpInst extends IRInstruction {
    public enum IcmpName {
        eq, ne, sgt, sge, slt, sle
    }

    private IcmpName operator;
    private IRType irType;
    private Operand op1;
    private Operand op2;
    private Register result;

    public IcmpInst(BasicBlock basicBlock, IcmpName operator, IRType irType,
                    Operand op1, Operand op2, Register result) {
        super(basicBlock);
        this.operator = operator;
        this.irType = irType;
        this.op1 = op1;
        this.op2 = op2;
        this.result = result;

        assert irType.equals(op1.getType()) || (op1 instanceof ConstNull && irType instanceof PointerType);
        assert irType.equals(op2.getType()) || (op2 instanceof ConstNull && irType instanceof PointerType);
        assert result.getType().equals(new IntegerType(IntegerType.BitWidth.int1));
    }

    @Override
    public void successfullyAdd() {
        result.setDef(this);
        op1.addUse(this);
        op2.addUse(this);
    }

    public IcmpName getOperator() {
        return operator;
    }

    public IRType getIrType() {
        return irType;
    }

    public Operand getOp1() {
        return op1;
    }

    public Operand getOp2() {
        return op2;
    }

    @Override
    public Register getResult() {
        return result;
    }

    public boolean shouldSwap(boolean assertOrNot) {
        if (assertOrNot)
            assert !(op1 instanceof Constant) || !(op2 instanceof Constant);
        else {
            if (op1 instanceof Constant && op2 instanceof Constant)
                return false;
        }
        return op1 instanceof Constant;
    }

    public void swapOps() {
        operator = operator == IcmpName.sgt ? IcmpName.slt
                : operator == IcmpName.slt ? IcmpName.sgt
                : operator == IcmpName.sge ? IcmpName.sle
                : operator == IcmpName.sle ? IcmpName.sge
                : operator;
        Operand tmp = op1;
        op1 = op2;
        op2 = tmp;
    }

    public void convertLeGeToLtGt() {
        if (op2 instanceof ConstBool)
            return;
        assert op2 instanceof ConstInt;
        if (operator == IcmpName.sle) {
            operator = IcmpName.slt;
            assert ((ConstInt) op2).getValue() != Integer.MAX_VALUE;
            ((ConstInt) op2).setValue(((ConstInt) op2).getValue() + 1);
        } else if (operator == IcmpName.sge) {
            operator = IcmpName.sgt;
            assert ((ConstInt) op2).getValue() != Integer.MIN_VALUE;
            ((ConstInt) op2).setValue(((ConstInt) op2).getValue() - 1);
        }
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (op1 == oldUse) {
            op1.removeUse(this);
            op1 = (Operand) newUse;
            op1.addUse(this);
        }
        if (op2 == oldUse) {
            op2.removeUse(this);
            op2 = (Operand) newUse;
            op2.addUse(this);
        }
    }

    @Override
    public void removeFromBlock() {
        op1.removeUse(this);
        op2.removeUse(this);
        super.removeFromBlock();
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        op1.markBaseAsLive(live, queue);
        op2.markBaseAsLive(live, queue);
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
        String instructionName = operator.name();
        ArrayList<String> operands = new ArrayList<>();
        operands.add(op1.toString());
        operands.add(op2.toString());
        return new CSE.Expression(instructionName, operands);
    }

    @Override
    public void clonedUseReplace(Map<BasicBlock, BasicBlock> blockMap, Map<Operand, Operand> operandMap) {
        if (op1 instanceof Parameter || op1 instanceof Register) {
            assert operandMap.containsKey(op1);
            op1 = operandMap.get(op1);
        }
        if (op2 instanceof Parameter || op2 instanceof Register) {
            assert operandMap.containsKey(op2);
            op2 = operandMap.get(op2);
        }
        op1.addUse(this);
        op2.addUse(this);
    }

    @Override
    public void addConstraintsForAndersen(Map<Operand, Andersen.Node> nodeMap, Set<Andersen.Node> nodes) {
        // Do nothing.
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
        if (licm.isLoopInvariant(result, loop))
            return false;

        if (licm.isLoopInvariant(op1, loop) && licm.isLoopInvariant(op2, loop)) {
            licm.markLoopInvariant(result);
            return true;
        }
        return false;
    }

    @Override
    public boolean canBeHoisted(LoopAnalysis.LoopNode loop) {
        return loop.defOutOfLoop(op1) && loop.defOutOfLoop(op2);
    }

    @Override
    public boolean combineInst(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        if (this.shouldSwap(false))
            this.swapOps();
        if (op1 == op2) { // The condition is so rigorous that this optimization almost do nothing.
            Operand replace;
            if (operator == IcmpName.eq || operator == IcmpName.sge || operator == IcmpName.sle)
                replace = new ConstBool(true);
            else
                replace = new ConstBool(false);

            for (IRInstruction instruction : result.getUse().keySet()) {
                if (!inQueue.contains(instruction)) {
                    queue.offer(instruction);
                    inQueue.add(instruction);
                }
            }
            result.replaceUse(replace);
            return true;
        } else if (op1.getType() instanceof IntegerType && (operator == IcmpName.sle || operator == IcmpName.sge)
                && !(op1 instanceof Constant) && (op2 instanceof Constant)) {
            assert op2 instanceof ConstInt;
            if ((operator == IcmpName.sle && ((ConstInt) op2).getValue() == Integer.MAX_VALUE)
                    || (operator == IcmpName.sge && ((ConstInt) op2).getValue() == Integer.MIN_VALUE)) {
                result.replaceUse(new ConstBool(true));
                for (IRInstruction instruction : result.getUse().keySet()) {
                    if (!inQueue.contains(instruction)) {
                        queue.offer(instruction);
                        inQueue.add(instruction);
                    }
                }
                return true;
            } else if ((operator == IcmpName.slt && ((ConstInt) op2).getValue() == Integer.MIN_VALUE)
                    || (operator == IcmpName.sgt && ((ConstInt) op2).getValue() == Integer.MAX_VALUE)) {
                result.replaceUse(new ConstBool(false));
                for (IRInstruction instruction : result.getUse().keySet()) {
                    if (!inQueue.contains(instruction)) {
                        queue.offer(instruction);
                        inQueue.add(instruction);
                    }
                }
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public String toString() {
        return result.toString() + " = icmp "
                + operator.name() + " " + irType.toString() + " " + op1.toString() + ", " + op2.toString();
    }

    @Override
    public Object clone() {
        IcmpInst icmpInst = (IcmpInst) super.clone();
        icmpInst.operator = this.operator;
        icmpInst.irType = this.irType;
        icmpInst.op1 = this.op1;
        icmpInst.op2 = this.op2;
        icmpInst.result = (Register) this.result.clone();

        icmpInst.result.setDef(icmpInst);
        return icmpInst;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
