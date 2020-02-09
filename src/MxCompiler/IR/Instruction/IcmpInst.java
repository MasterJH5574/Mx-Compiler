package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Operand.Operand;

public class IcmpInst extends IRInstruction {
    public enum IcmpName {
        eq, ne, sgt, sge, slt, sle
    }

    private IcmpName cond;
    private Operand op1;
    private Operand op2;
    private Operand result;

    public IcmpInst(BasicBlock basicBlock, IcmpName cond, Operand op1, Operand op2, Operand result) {
        super(basicBlock);
        this.cond = cond;
        this.op1 = op1;
        this.op2 = op2;
        this.result = result;
    }

    public IcmpName getCond() {
        return cond;
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
}
