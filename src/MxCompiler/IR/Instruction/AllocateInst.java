package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.IRType;

public class AllocateInst extends IRInstruction {
    private Register result;
    private IRType type;

    public AllocateInst(BasicBlock basicBlock, Register result, IRType type) {
        // Note that "type" here should be converted to a pointer
        super(basicBlock);
        this.result = result;
        this.type = type;

        assert (new PointerType(type)).equals(result.getType());
        result.setDef(this);
    }

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
    public String toString() {
        return result.toString() + " = alloca " + type.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
