package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.VoidType;

import java.util.ArrayList;
import java.util.Queue;
import java.util.Set;

public class CallInst extends IRInstruction {
    private Function function;
    private ArrayList<Operand> parameters;
    private Operand result;

    public CallInst(BasicBlock basicBlock, Function function, ArrayList<Operand> parameters, Register result) {
        super(basicBlock);
        this.function = function;
        this.parameters = parameters;
        this.result = result;

        if (result != null)
            assert result.getType().equals(function.getFunctionType().getReturnType());
        else
            assert function.getFunctionType().getReturnType().equals(new VoidType());

        assert parameters.size() == function.getParameters().size();
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i) instanceof ConstNull) {
                assert function.getParameters().get(i).getType() instanceof PointerType;
                assert function.getFunctionType().getParameterList().get(i) instanceof PointerType;
                assert function.getParameters().get(i).getType()
                        .equals(function.getFunctionType().getParameterList().get(i));
            } else {
                assert parameters.get(i).getType().equals(function.getParameters().get(i).getType());
                assert parameters.get(i).getType().equals(function.getFunctionType().getParameterList().get(i));
            }
        }
    }

    @Override
    public void successfullyAdd() {
        for (Operand parameter : parameters)
            parameter.addUse(this);

        if (result != null)
            ((Register) result).setDef(this);

        function.addUse(this);
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

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (function == oldUse) {
            function = (Function) newUse;
            function.addUse(this);
        } else {
            for (int i = 0; i < parameters.size(); i++)
                if (parameters.get(i) == oldUse) {
                    parameters.set(i, (Operand) newUse);
                    parameters.get(i).addUse(this);
                }
        }
    }

    @Override
    public void removeFromBlock() {
        for (Operand parameter : parameters)
            parameter.removeUse(this);
        function.removeUse(this);
        super.removeFromBlock();
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        for (Operand parameter : parameters)
            parameter.markAsLive(live, queue);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        if (result != null) {
            assert !(function.getFunctionType().getReturnType() instanceof VoidType);
            string.append(result.toString()).append(" = ");
        } else
            assert function.getFunctionType().getReturnType() instanceof VoidType;
        string.append("call ");
        string.append(function.getFunctionType().getReturnType().toString()).append(" ");
        string.append("@").append(function.getName()).append("(");
        for (int i = 0; i < parameters.size(); i++) {
            string.append(function.getParameters().get(i).getType()).append(" ").append(parameters.get(i).toString());
            if (i != parameters.size() - 1)
                string.append(", ");
        }
        string.append(")");
        return string.toString();
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
