package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.Optim.Andersen;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.SCCP;
import MxCompiler.Optim.SideEffectChecker;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BinaryOpInst extends IRInstruction {
    public enum BinaryOpName {
        add, sub, mul, sdiv, srem,          // Binary Operations
        shl, ashr, and, or, xor             // Bitwise Binary Operations
    }

    private BinaryOpName op;
    private Operand lhs;
    private Operand rhs;
    private Register result;

    public BinaryOpInst(BasicBlock basicBlock, BinaryOpName op, Operand lhs, Operand rhs, Register result) {
        super(basicBlock);
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
        this.result = result;

        assert lhs.getType().equals(result.getType());
        assert rhs.getType().equals(result.getType());
        assert !(result.getType() instanceof PointerType);
    }

    @Override
    public void successfullyAdd() {
        result.setDef(this);
        lhs.addUse(this);
        rhs.addUse(this);
    }

    public BinaryOpName getOp() {
        return op;
    }

    public Operand getLhs() {
        return lhs;
    }

    public Operand getRhs() {
        return rhs;
    }

    @Override
    public Register getResult() {
        return result;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (lhs == oldUse) {
            lhs = (Operand) newUse;
            lhs.addUse(this);
        }
        if (rhs == oldUse) {
            rhs = (Operand) newUse;
            rhs.addUse(this);
        }
    }

    @Override
    public void removeFromBlock() {
        lhs.removeUse(this);
        rhs.removeUse(this);
        super.removeFromBlock();
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        lhs.markBaseAsLive(live, queue);
        rhs.markBaseAsLive(live, queue);
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
        String instructionName = op.name();
        ArrayList<String> operands = new ArrayList<>();
        operands.add(lhs.toString());
        operands.add(rhs.toString());
        return new CSE.Expression(instructionName, operands);
    }

    @Override
    public void clonedUseReplace(Map<BasicBlock, BasicBlock> blockMap, Map<Operand, Operand> operandMap) {
        if (lhs instanceof Parameter || lhs instanceof Register) {
            assert operandMap.containsKey(lhs);
            lhs = operandMap.get(lhs);
        }
        if (rhs instanceof Parameter || rhs instanceof Register) {
            assert operandMap.containsKey(rhs);
            rhs = operandMap.get(rhs);
        }
        lhs.addUse(this);
        rhs.addUse(this);
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
    public String toString() {
        return result.toString() + " = " +
                op.name() + " " + result.getType().toString() + " " + lhs.toString() + ", " + rhs.toString();
    }

    @Override
    public Object clone() {
        BinaryOpInst binaryOpInst = (BinaryOpInst) super.clone();
        binaryOpInst.op = this.op;
        binaryOpInst.lhs = this.lhs;
        binaryOpInst.rhs = this.rhs;
        binaryOpInst.result = (Register) this.result.clone();

        binaryOpInst.result.setDef(binaryOpInst);
        return binaryOpInst;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
