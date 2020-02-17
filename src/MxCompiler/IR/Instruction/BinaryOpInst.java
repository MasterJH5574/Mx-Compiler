package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;

public class BinaryOpInst extends IRInstruction {
    public enum BinaryOpName {
        add, sub, mul, sdiv, srem,          // Binary Operations
        shl, ashr, and, or, xor             // Bitwise Binary Operations
    }

    private BinaryOpName op;
    private Operand lhs;
    private Operand rhs;
    private Operand result;

    public BinaryOpInst(BasicBlock basicBlock, BinaryOpName op, Operand lhs, Operand rhs, Register result) {
        super(basicBlock);
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
        this.result = result;

        assert lhs.getType().equals(result.getType());
        assert rhs.getType().equals(result.getType());

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

    public Operand getResult() {
        return result;
    }

    @Override
    public String toString() {
        return result.toString() + " = " +
                op.name() + " " + result.getType().toString() + " " + lhs.toString() + ", " + rhs.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
