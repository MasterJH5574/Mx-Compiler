package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;

import java.util.ArrayList;

public class GetElementPtrInst extends IRInstruction {
    private IRType pointerType;
    private ArrayList<Operand> index;

    public GetElementPtrInst(BasicBlock basicBlock, IRType pointerType, ArrayList<Operand> index) {
        super(basicBlock);
        this.pointerType = pointerType;
        this.index = index;
    }

    public IRType getPointerType() {
        return pointerType;
    }

    public ArrayList<Operand> getIndex() {
        return index;
    }
}
