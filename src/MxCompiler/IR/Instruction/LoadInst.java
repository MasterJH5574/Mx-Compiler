package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.GlobalVariable;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.PointerType;

import java.util.Queue;
import java.util.Set;

public class LoadInst extends IRInstruction {
    private IRType type;
    private Operand pointer;
    private Operand result;

    public LoadInst(BasicBlock basicBlock, IRType type, Operand pointer, Register result) {
        super(basicBlock);
        this.type = type;
        this.pointer = pointer;
        this.result = result;

        if (pointer instanceof GlobalVariable)
            assert pointer.getType().equals(type);
        else {
            assert pointer.getType() instanceof PointerType;
            assert ((PointerType) pointer.getType()).getBaseType().equals(type);
        }
        assert result.getType().equals(type);
    }

    @Override
    public void successfullyAdd() {
        ((Register) result).setDef(this);
        pointer.addUse(this);
    }

    public IRType getType() {
        return type;
    }

    public Operand getPointer() {
        return pointer;
    }

    public Operand getResult() {
        return result;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (pointer == oldUse) {
            pointer = (Operand) newUse;
            pointer.addUse(this);
        }
    }

    @Override
    public void removeFromBlock() {
        pointer.removeUse(this);
        super.removeFromBlock();
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        pointer.markAsLive(live, queue);
    }

    @Override
    public String toString() {
        if (pointer instanceof GlobalVariable)
            return result.toString() + " = load " + type.toString() +
                    ", " + (new PointerType(pointer.getType())).toString() + " " + pointer.toString();
        else
            return result.toString() + " = load "
                    + type.toString() + ", " + pointer.getType().toString() + " " + pointer.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
