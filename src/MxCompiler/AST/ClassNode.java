package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class ClassNode extends programUnitNode {
    private String identifier;
    private ArrayList<VarNode> varList = new ArrayList<>();
    private ArrayList<FunctionNode> funcList = new ArrayList<>();
    // Regard constructor as a method with return-type void.

    public ClassNode(Location location, String identifier) {
        super(location);
        this.identifier = identifier;
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
