package MxCompiler.IR.TypeSystem;

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
}
