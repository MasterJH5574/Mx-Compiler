// ------ Common Subexpression Elimination ------

package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CSE extends Pass {
    private Map<Expression, ArrayList<Register>> expressionMap;

    static public class Expression {
        private String instructionName;
        private ArrayList<String> operands;

        public Expression(String instructionName, ArrayList<String> operands) {
            this.instructionName = instructionName;
            this.operands = operands;
        }

        public String getInstructionName() {
            return instructionName;
        }

        public boolean isCommutable() {
            return instructionName.equals("add")
                    || instructionName.equals("mul")
                    || instructionName.equals("and")
                    || instructionName.equals("or")
                    || instructionName.equals("xor")
                    || instructionName.equals("eq")
                    || instructionName.equals("ne")
                    || instructionName.equals("sgt")
                    || instructionName.equals("sge")
                    || instructionName.equals("slt")
                    || instructionName.equals("sle");
        }

        public Expression getCommutation() {
            assert operands.size() == 2;
            ArrayList<String> newOperands = new ArrayList<>();
            newOperands.add(operands.get(1));
            newOperands.add(operands.get(0));
            switch (instructionName) {
                case "sgt":
                    return new Expression("slt", newOperands);
                case "sge":
                    return new Expression("sle", newOperands);
                case "slt":
                    return new Expression("sgt", newOperands);
                case "sle":
                    return new Expression("sge", newOperands);
                default:
                    return new Expression(instructionName, newOperands);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Expression))
                return false;
            if (!((Expression) obj).getInstructionName().equals(this.instructionName))
                return false;
            if (((Expression) obj).operands.size() != this.operands.size())
                return false;
            for (int i = 0; i < this.operands.size(); i++) {
                if (!((Expression) obj).operands.get(i).equals(this.operands.get(i)))
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    public CSE(Module module) {
        super(module);
    }

    @Override
    public boolean run() {
        changed = false;
        for (Function function : module.getFunctionMap().values())
            changed |= commonSubexpressionElimination(function);
        return false;
    }

    private boolean commonSubexpressionElimination(Function function) {
        if (function.isNotFunctional())
            return false;
        boolean changed = false;
        expressionMap = new HashMap<>();
        ArrayList<BasicBlock> blocks = function.getDFSOrder();
        for (BasicBlock block : blocks)
            changed |= commonSubexpressionElimination(block);
        return changed;
    }

    private boolean commonSubexpressionElimination(BasicBlock block) {
        boolean changed = false;
        IRInstruction ptr = block.getInstHead();
        while (ptr != null) {
            IRInstruction next = ptr.getInstNext();
            if (ptr.canConvertToExpression()) {
                Expression expression = ptr.convertToExpression();
                Register register = lookupExpression(expression, block);
                if (register != null) {
                    ptr.getResult().replaceUse(register);
                    ptr.removeFromBlock();
                    changed = true;
                } else {
                    putExpression(expression, ptr.getResult());
                    if (expression.isCommutable())
                        putExpression(expression.getCommutation(), ptr.getResult());
                }
            }
            ptr = next;
        }
        return changed;
    }

    private Register lookupExpression(Expression expression, BasicBlock block) {
        if (!expressionMap.containsKey(expression))
            return null;
        ArrayList<Register> registers = expressionMap.get(expression);
        for (Register register : registers) {
            if (block == register.getDef().getBasicBlock()
                    ||  block.getStrictDominators().contains(register.getDef().getBasicBlock()))
                return register;
        }
        return null;
    }

    private void putExpression(Expression expression, Register register) {
        if (!expressionMap.containsKey(expression))
            expressionMap.put(expression, new ArrayList<>());
        expressionMap.get(expression).add(register);
    }
}
