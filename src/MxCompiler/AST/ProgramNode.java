package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class ProgramNode extends ASTNode {
    private ArrayList<programUnitNode> programUnits = new ArrayList<>();

    public ProgramNode(Location location) {
        super(location);
    }

    public ArrayList<programUnitNode> getProgramUnits() {
        return programUnits;
    }

    public void addObject(programUnitNode programUnit) {
        programUnits.add(programUnit);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
