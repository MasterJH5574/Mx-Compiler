package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IntegerType;
import MxCompiler.IR.TypeSystem.PointerType;

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
    private Operand result;

    public IcmpInst(BasicBlock basicBlock, IcmpName operator, IRType irType, Operand op1, Operand op2, Register result) {
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
        ((Register) result).setDef(this);
        op1.addUse(this);
        op2.addUse(this);
    }

    public IcmpName getOperator() {
        return operator;
    }

    public Operand getOp1() {
        return op1;
    }

    public Operand getOp2() {
        return op2;
    }

    public Operand getResult() {
        return result;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (op1 == oldUse) {
            op1 = (Operand) newUse;
            op1.addUse(this);
        }
        if (op2 == oldUse) {
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
        op1.markAsLive(live, queue);
        op2.markAsLive(live, queue);
    }

    @Override
    public String toString() {
        return result.toString() + " = icmp "
                + operator.name() + " " + irType.toString() + " " + op1.toString() + ", " + op2.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
