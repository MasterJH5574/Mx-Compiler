package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;

public class BitCastToInst extends IRInstruction {
    private Operand src;
    private IRType objectType;
    private Operand result;

    public BitCastToInst(BasicBlock basicBlock, Operand src, IRType objectType, Operand result) {
        super(basicBlock);
        this.src = src;
        this.objectType = objectType;
        this.result = result;
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
}
