package MxCompiler.IR.TypeSystem;

import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.Utilities.Aligner;

import java.util.ArrayList;

public class StructureType extends IRType {
    private String name;
    private ArrayList<IRType> memberList;

    public StructureType(String name, ArrayList<IRType> memberList) {
        this.name = name;
        this.memberList = memberList;
    }

    public String getName() {
        return name;
    }

    public ArrayList<IRType> getMemberList() {
        return memberList;
    }

    @Override
    public Operand getDefaultValue() {
        // This method will never be called.
        throw new RuntimeException();
    }

    @Override
    public int getBytes() {
        int size = 0;
        int max = 0;
        for (IRType irType : memberList) {
            int typeSize = irType.getBytes();
            size = Aligner.align(size, typeSize) + typeSize;
            max = Math.max(max, typeSize);
        }
        size = Aligner.align(size, max);
        return size;
    }

    public String structureToString() {
        StringBuilder string = new StringBuilder(this.toString());
        string.append(" = type { ");
        for (int i = 0; i < memberList.size(); i++) {
            string.append(memberList.get(i).toString());
            if (i != memberList.size() - 1)
                string.append(", ");
        }
        string.append(" }");
        return string.toString();
    }

    @Override
    public String toString() {
        return "%" + name;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
