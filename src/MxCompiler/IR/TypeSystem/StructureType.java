package MxCompiler.IR.TypeSystem;

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
}
