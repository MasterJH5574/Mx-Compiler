package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Operand.Operand;

import java.util.ArrayList;

public class CallInst extends IRInstruction {
    private Function function;
    private ArrayList<Operand> parameters;
    private Operand result;

    public CallInst(BasicBlock basicBlock, Function function, ArrayList<Operand> parameters, Operand result) {
        super(basicBlock);
        this.function = function;
        this.parameters = parameters;
        this.result = result;
    }

    public Function getFunction() {
        return function;
    }

    public ArrayList<Operand> getParameters() {
        return parameters;
    }

    public Operand getResult() {
        return result;
    }
}
