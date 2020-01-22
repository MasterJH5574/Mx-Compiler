package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class VarDeclStmtNode extends StmtNode {
    private ArrayList<VarNode> varList = new ArrayList<>();

    public VarDeclStmtNode(Location location) {
        super(location);
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
