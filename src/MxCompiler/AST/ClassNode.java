package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class ClassNode extends ProgramUnitNode {
    private String identifier;
    private ArrayList<VarNode> varList;
    private ArrayList<FunctionNode> funcList;
    // Regard constructor as a method with return-type void.

    public ClassNode(Location location, String identifier,
                     ArrayList<VarNode> varList, ArrayList<FunctionNode> funcList) {
        super(location);
        this.identifier = identifier;
        this.varList = varList;
        this.funcList = funcList;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ArrayList<VarNode> getVarList() {
        return varList;
    }

    public void addVar(VarNode var) {
        varList.add(var);
    }

    public ArrayList<FunctionNode> getFuncList() {
        return funcList;
    }

    public void addFunction(FunctionNode function) {
        funcList.add(function);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
