package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class VarDeclStmtNode extends StmtNode {
    private ArrayList<VarNode> varList;

    public VarDeclStmtNode(Location location, ArrayList<VarNode> varList) {
        super(location);
        this.varList = varList;
    }

    public ArrayList<VarNode> getVarList() {
        return varList;
    }

    public void addVar(VarNode var) {
        varList.add(var);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("<VarDeclStmtNode>\n");
        string.append("varList:\n");
        for (VarNode var : varList)
            string.append(var.toString());
        return string.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
