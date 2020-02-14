package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.PointerType;

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

        assert result.getType().equals(pointer.getType());
        assert pointer.getType() instanceof PointerType;
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

    @Override
    public String toString() {
        IRType baseType = ((PointerType) pointer.getType()).getBaseType();
        StringBuilder string = new StringBuilder();
        string.append(result.toString()).append(" = ");
        string.append("getelementptr ").append(baseType.toString()).append(", ");
        string.append(pointer.getType().toString()).append(" ").append(pointer.toString());
        for (Operand aIndex : index)
            string.append(", ").append(aIndex.toString());
        return string.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
