package MxCompiler.IR.TypeSystem;

import MxCompiler.Entity.VariableEntity;
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

    @Override
    public String toString() {
        return "%" + name;
    }
}
