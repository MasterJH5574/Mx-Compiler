package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;

public class ReturnInst extends IRInstruction {
    private IRType type; // void or not
    private Operand returnValue;

    public ReturnInst(BasicBlock basicBlock, IRType type, Operand returnValue) {
        super(basicBlock);
        this.type = type;
        this.returnValue = returnValue;
    }

    public IRType getType() {
        return type;
    }

    public Operand getReturnValue() {
        return returnValue;
    }
}
