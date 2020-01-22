package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class ProgramNode extends ASTNode {
    private ArrayList<ProgramUnitNode> programUnits = new ArrayList<>();

    public ProgramNode(Location location) {
        super(location);
    }

    public ArrayList<ProgramUnitNode> getProgramUnits() {
        return programUnits;
    }

    public void addProgramUnit(ProgramUnitNode programUnit) {
        programUnits.add(programUnit);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
