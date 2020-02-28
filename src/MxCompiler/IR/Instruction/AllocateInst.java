package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.SCCP;

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
    public String toString() {
        return result.toString() + " = alloca " + type.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
