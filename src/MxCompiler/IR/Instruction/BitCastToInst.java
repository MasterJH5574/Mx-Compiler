package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.IRType;

public class BitCastToInst extends IRInstruction {
    private Operand src;
    private IRType objectType;
    private Operand result;

    public BitCastToInst(BasicBlock basicBlock, Operand src, IRType objectType, Register result) {
        super(basicBlock);
        this.src = src;
        this.objectType = objectType;
        this.result = result;
    }

    @Override
    public void successfullyAdd() {
        ((Register) result).setDef(this);
        src.addUse(this);
    }

    public Operand getSrc() {
        return src;
    }

    public IRType getObjectType() {
        return objectType;
    }

    public Operand getResult() {
        return result;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (src == oldUse) {
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
    public String toString() {
        return result.toString() + " = bitcast "
                + src.getType().toString() + " " + src.toString() + " to " + objectType.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
