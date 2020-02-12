package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;

import java.util.ArrayList;

public class GetElementPtrInst extends IRInstruction {
    private Operand pointer;
    private ArrayList<Operand> index;
    private Operand result;

    public GetElementPtrInst(BasicBlock basicBlock, Operand pointer, ArrayList<Operand> index, Operand result) {
        super(basicBlock);
        this.pointer = pointer;
        this.index = index;
        this.result = result;
    }

    public Operand getPointer() {
        return pointer;
    }

    public ArrayList<Operand> getIndex() {
        return index;
    }

    public Operand getResult() {
        return result;
    }
}
