package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Operand.Operand;

public class BitCastToInst extends IRInstruction {
    private Operand src;
    private Operand objectType;
    private Operand result;

    public BitCastToInst(BasicBlock basicBlock, Operand src, Operand objectType, Operand result) {
        super(basicBlock);
        this.src = src;
        this.objectType = objectType;
        this.result = result;
    }

    public Operand getSrc() {
        return src;
    }

    public Operand getObjectType() {
        return objectType;
    }

    public Operand getResult() {
        return result;
    }
}
