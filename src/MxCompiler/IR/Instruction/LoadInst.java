package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;

public class LoadInst extends IRInstruction {
    private IRType type;
    private Operand pointer;
    private Operand result;

    public LoadInst(BasicBlock basicBlock, IRType type, Operand pointer, Operand result) {
        super(basicBlock);
        this.type = type;
        this.pointer = pointer;
        this.result = result;
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
}
