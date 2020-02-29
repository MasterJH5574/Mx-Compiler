package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.SCCP;

import java.util.ArrayList;
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
