package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class VarNodeList extends ProgramUnitNode {
    private ArrayList<VarNode> varNodes = new ArrayList<>();

    public VarNodeList(Location location) {
        super(location);
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
