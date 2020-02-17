package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IntegerType;
import MxCompiler.IR.TypeSystem.PointerType;

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

        result.setDef(this);
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
    public String toString() {
        return result.toString() + " = icmp "
                + operator.name() + " " + irType.toString() + " " + op1.toString() + ", " + op2.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
