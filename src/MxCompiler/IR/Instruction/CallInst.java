package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Operand.Operand;

import java.util.ArrayList;

public class CallInst extends IRInstruction {
    private Function function;
    private ArrayList<Operand> parameters;

    public CallInst(BasicBlock basicBlock, Function function, ArrayList<Operand> parameters) {
        super(basicBlock);
        this.function = function;
        this.parameters = parameters;
    }

    public Function getFunction() {
        return function;
    }

    public ArrayList<Operand> getParameters() {
        return parameters;
    }
}
