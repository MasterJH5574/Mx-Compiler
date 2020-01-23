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
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
