package MxCompiler.IR.TypeSystem;

import MxCompiler.IR.IRVisitor;

import java.util.ArrayList;

public class FunctionType extends IRType {
    private IRType returnType;
    private ArrayList<IRType> parameterList;

    public FunctionType(IRType returnType, ArrayList<IRType> parameterList) {
        this.returnType = returnType;
        this.parameterList = parameterList;
    }

    public IRType getReturnType() {
        return returnType;
    }

    public ArrayList<IRType> getParameterList() {
        return parameterList;
    }

    @Override
    public int getBytes() {
        return 0;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("FunctionType: ").append(returnType.toString()).append(" (");
        for (int i = 0; i < parameterList.size(); i++) {
            string.append(parameterList.get(i).toString());
            if (i != parameterList.size() - 1)
                string.append(", ");
        }
        string.append(")\n");
        return string.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
