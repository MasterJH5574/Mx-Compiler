package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class ProgramNode extends ASTNode {
    private ArrayList<ProgramUnitNode> programUnits;

    public ProgramNode(Location location, ArrayList<ProgramUnitNode> programUnits) {
        super(location);
        this.programUnits = programUnits;
    }

    public ArrayList<ProgramUnitNode> getProgramUnits() {
        return programUnits;
    }

    public void addProgramUnit(ProgramUnitNode programUnit) {
        programUnits.add(programUnit);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("<ProgramNode>\n");
        string.append("programUnits:\n");
        for (ProgramUnitNode unit : programUnits)
            string.append(unit.toString());
        return string.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }
}
