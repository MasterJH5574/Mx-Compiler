package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.VoidType;

public class ReturnInst extends IRInstruction {
    private IRType type; // void or not
    private Operand returnValue;

    public ReturnInst(BasicBlock basicBlock, IRType type, Operand returnValue) {
        super(basicBlock);
        this.type = type;
        this.returnValue = returnValue;

        if (!(type instanceof VoidType))
            assert type.equals(returnValue.getType())
                    || (returnValue instanceof ConstNull && type instanceof PointerType);
        else
            assert returnValue == null;
    }

    public IRType getType() {
        return type;
    }

    public Operand getReturnValue() {
        return returnValue;
    }

    @Override
    public String toString() {
        if (!(type instanceof VoidType))
            return "ret " + type.toString() + " " + returnValue.toString();
        else
            return "ret void";
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
