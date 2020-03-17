// ------ Common Subexpression Elimination ------

package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.CallInst;
import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Instruction.LoadInst;
import MxCompiler.IR.Instruction.StoreInst;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;

import java.util.*;

public class CSE extends Pass {
    private Andersen andersen;
    private SideEffectChecker sideEffectChecker;
    private Map<Expression, ArrayList<Register>> expressionMap;
    private Map<LoadInst, Set<IRInstruction>> unavailable;

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

    public CSE(Module module, Andersen andersen, SideEffectChecker sideEffectChecker) {
        super(module);
        this.andersen = andersen;
        this.sideEffectChecker = sideEffectChecker;
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional())
                return false;
        }

        sideEffectChecker.setIgnoreIO(true);
        sideEffectChecker.setIgnoreLoad(true);
        sideEffectChecker.run();

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
        unavailable = new HashMap<>();

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
                Register register = lookupExpression(expression, ptr, block);
                if (register != null) {
                    ptr.getResult().replaceUse(register);
                    ptr.removeFromBlock();
                    changed = true;
                } else {
                    putExpression(expression, ptr.getResult());
                    if (expression.isCommutable())
                        putExpression(expression.getCommutation(), ptr.getResult());

                    if (ptr instanceof LoadInst)
                        propagateUnavailability((LoadInst) ptr);
                }
            }
            ptr = next;
        }
        return changed;
    }

    private Register lookupExpression(Expression expression, IRInstruction instruction, BasicBlock block) {
        if (!expressionMap.containsKey(expression))
            return null;
        ArrayList<Register> registers = expressionMap.get(expression);
        for (Register register : registers) {
            IRInstruction def = register.getDef();
            if (expression.instructionName.equals("load")) {
                assert def instanceof LoadInst;
                assert unavailable.containsKey(def);
                if (def.getBasicBlock().dominate(block) && !unavailable.get(def).contains(instruction))
                    return register;
            } else {
                if (def.getBasicBlock().dominate(block))
                    return register;
            }
        }
        return null;
    }

    private void putExpression(Expression expression, Register register) {
        if (!expressionMap.containsKey(expression))
            expressionMap.put(expression, new ArrayList<>());
        expressionMap.get(expression).add(register);
    }

    private void markSuccessorUnavailable(LoadInst loadInst, IRInstruction instruction,
                                          Set<IRInstruction> unavailable, Queue<IRInstruction> queue) {
        BasicBlock block = instruction.getBasicBlock();
        if (instruction == block.getInstTail()) {
            for (BasicBlock successor : block.getSuccessors()) {
                if (successor.getStrictDominators().contains(loadInst.getBasicBlock())) {
                    IRInstruction instHead = successor.getInstHead();
                    if (!unavailable.contains(instHead)) {
                        unavailable.add(instHead);
                        queue.offer(instHead);
                    }
                }
            }
        } else {
            IRInstruction instNext = instruction.getInstNext();
            if (!unavailable.contains(instNext)) {
                unavailable.add(instNext);
                queue.offer(instNext);
            }
        }
    }

    private void propagateUnavailability(LoadInst loadInst) {
        Set<IRInstruction> unavailable = new HashSet<>();
        Queue<IRInstruction> queue = new LinkedList<>();

        Operand loadPointer = loadInst.getPointer();
        BasicBlock loadBlock = loadInst.getBasicBlock();
        Function function = loadBlock.getFunction();

        for (BasicBlock block : function.getBlocks()) {
            if (loadInst.getBasicBlock().dominate(block)) {
                IRInstruction ptr = loadBlock == block ? loadInst.getInstNext() : block.getInstHead();
                while (ptr != null) {
                    if (ptr instanceof StoreInst) {
                        if (andersen.mayAlias(loadPointer, ((StoreInst) ptr).getPointer()))
                            markSuccessorUnavailable(loadInst, ptr, unavailable, queue);
                    } else if (ptr instanceof CallInst) {
                        Function callee = ((CallInst) ptr).getFunction();
                        if (sideEffectChecker.hasSideEffect(callee))
                            markSuccessorUnavailable(loadInst, ptr, unavailable, queue);
                    }
                    ptr = ptr.getInstNext();
                }
            }
        }

        while (!queue.isEmpty()) {
            IRInstruction inst = queue.poll();
            markSuccessorUnavailable(loadInst, inst, unavailable, queue);
        }
        this.unavailable.put(loadInst, unavailable);
    }
}
