package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class VarNodeList extends ProgramUnitNode {
    private ArrayList<VarNode> varNodes;

    public VarNodeList(Location location, ArrayList<VarNode> varNodes) {
        super(location);
        this.varNodes = varNodes;
    }

    public ArrayList<VarNode> getVarNodes() {
        return varNodes;
    }

    public void addVarNode(VarNode varNode) {
        varNodes.add(varNode);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
