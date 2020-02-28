// ------ Sparse Conditional Constant Propagation ------

package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.IntegerType;
import MxCompiler.Utilities.Pair;

import java.util.*;

public class SCCP extends Pass implements IRVisitor {
    public static class Status {
        public enum OperandStatus {
            undefined, constant, multiDefined
        }

        private OperandStatus operandStatus;
        private Operand operand;

        public Status(OperandStatus operandStatus, Operand operand) {
            this.operandStatus = operandStatus;
            this.operand = operand;
        }

        public OperandStatus getOperandStatus() {
            return operandStatus;
        }

        public Operand getOperand() {
            return operand;
        }

        @Override
        public String toString() {
            if (operandStatus == OperandStatus.constant)
                return "constant " + operand.toString();
            else
                return operandStatus.name();
        }
    }

    private Queue<Register> registerQueue;
    private Queue<BasicBlock> blockQueue;
    private Map<Operand, Status> operandLattice;
    private Set<BasicBlock> blockExecutable;

    public SCCP(Module module) {
        super(module);
    }

    @Override
    public boolean run() {
        changed = false;
        for (Function function : module.getFunctionMap().values())
            visit(function);
        return changed;
    }

    private void markExecutable(BasicBlock block) {
        if (!blockExecutable.contains(block)) {
            blockExecutable.add(block);
            blockQueue.offer(block);
        } else {
            // Update phi instructions.
            IRInstruction ptr = block.getInstHead();
            while (ptr instanceof PhiInst) {
                ptr.accept(this);
                ptr = ptr.getInstNext();
            }
        }
    }

    private void markConstant(Register register, Constant constant) {
        Status status = new Status(Status.OperandStatus.constant, constant);
        Status oldStatus = getStatus(register);
        if (oldStatus.operandStatus == Status.OperandStatus.undefined) {
            operandLattice.replace(register, status);
            registerQueue.offer(register);
        } else {
            assert oldStatus.operandStatus != Status.OperandStatus.multiDefined
                    && oldStatus.operand.equals(status.operand);
        }
    }

    private void markMultiDefined(Register register) {
        Status oldStatus = getStatus(register);
        if (oldStatus.operandStatus != Status.OperandStatus.multiDefined) {
            operandLattice.replace(register, new Status(Status.OperandStatus.multiDefined, null));
            registerQueue.offer(register);
        }
    }

    public Status getStatus(Operand operand) {
        if (operandLattice.containsKey(operand))
            return operandLattice.get(operand);
        Status res;
        if (operand.isConstValue())
            res = new Status(Status.OperandStatus.constant, operand);
        else if (operand instanceof Parameter)
            res = new Status(Status.OperandStatus.multiDefined, null);
        else
            res = new Status(Status.OperandStatus.undefined, null);
        operandLattice.put(operand, res);
        return res;
    }

    @Override
    public void visit(Module module) {
        // This method will never be called.
    }

    @Override
    public void visit(Function function) {
        registerQueue = new LinkedList<>();
        blockQueue = new LinkedList<>();
        operandLattice = new HashMap<>();
        blockExecutable = new HashSet<>();

        markExecutable(function.getEntranceBlock());
        while (!registerQueue.isEmpty() || !blockQueue.isEmpty()) {
            while (!blockQueue.isEmpty()) {
                BasicBlock block = blockQueue.poll();
                block.accept(this); // visit BasicBlock
            }

            while (!registerQueue.isEmpty()) {
                Register register = registerQueue.poll();
                assert operandLattice.containsKey(register);
                for (IRInstruction instruction : register.getUse().keySet()) {
                    assert register.getUse().get(instruction) != 0;
                    instruction.accept(this); // visit IRInstruction
                }
            }
        }

        boolean functionChanged = false;
        ArrayList<BasicBlock> blocks = function.getBlocks();
        for (BasicBlock block : blocks)
            functionChanged |= replaceRegisterWithConstant(block);

        changed |= functionChanged;
    }

    @Override
    public void visit(BasicBlock block) {
        ArrayList<IRInstruction> instructions = block.getInstructions();
        for (IRInstruction instruction : instructions)
            instruction.accept(this); // visit IRInstruction
    }

    @Override
    public void visit(ReturnInst inst) {
        // Do nothing.
    }

    @Override
    public void visit(BranchInst inst) {
        if (!inst.isConditional())
            markExecutable(inst.getThenBlock());
        else {
            Operand cond = inst.getCond();
            Status condStatus = getStatus(cond);

            if (condStatus.operandStatus == Status.OperandStatus.constant) {
                if (((ConstBool) condStatus.operand).getValue())
                    markExecutable(inst.getThenBlock());
                else
                    markExecutable(inst.getElseBlock());
            } else if (condStatus.operandStatus == Status.OperandStatus.multiDefined) {
                markExecutable(inst.getThenBlock());
                markExecutable(inst.getElseBlock());
            }
        }
    }

    @Override
    public void visit(BinaryOpInst inst) {
        Operand lhs = inst.getLhs();
        Operand rhs = inst.getRhs();
        Status lhsStatus = getStatus(lhs);
        Status rhsStatus = getStatus(rhs);

        if (lhsStatus.operandStatus == Status.OperandStatus.constant
                && rhsStatus.operandStatus == Status.OperandStatus.constant) {
            assert lhsStatus.operand instanceof Constant;
            assert rhsStatus.operand instanceof Constant;
            Constant foldResult = foldConstant(inst, (Constant) lhsStatus.operand, (Constant) rhsStatus.operand);
            if (foldResult != null) {
                // If the binary operation will cause any error, foldResult will be null.
                markConstant(inst.getResult(), foldResult);
            }
        } else if (lhsStatus.operandStatus == Status.OperandStatus.multiDefined
                || rhsStatus.operandStatus == Status.OperandStatus.multiDefined)
            markMultiDefined(inst.getResult());
    }

    @Override
    public void visit(LoadInst inst) {
        markMultiDefined(inst.getResult());
    }

    @Override
    public void visit(StoreInst inst) {
        // Do nothing.
    }

    @Override
    public void visit(AllocateInst inst) {
        markMultiDefined(inst.getResult());
    }

    @Override
    public void visit(GetElementPtrInst inst) {
        markMultiDefined(inst.getResult());
    }

    @Override
    public void visit(BitCastToInst inst) {
        Status srcStatus = getStatus(inst.getSrc());
        if (srcStatus.operandStatus == Status.OperandStatus.constant) {
            Constant constant;
            if (srcStatus.operand instanceof ConstNull)
                constant = new ConstNull();
            else
                constant = ((Constant) srcStatus.operand).castToType(inst.getObjectType());
            markConstant(inst.getResult(), constant);
        } else if (srcStatus.operandStatus == Status.OperandStatus.multiDefined)
            markMultiDefined(inst.getResult());
    }

    @Override
    public void visit(IcmpInst inst) {
        Operand op1 = inst.getOp1();
        Operand op2 = inst.getOp2();
        Status op1Status = getStatus(op1);
        Status op2Status = getStatus(op2);

        if (op1Status.operandStatus == Status.OperandStatus.constant
                && op2Status.operandStatus == Status.OperandStatus.constant) {
            assert op1Status.operand instanceof Constant;
            assert op2Status.operand instanceof Constant;
            Constant foldResult = foldConstant(inst, (Constant) op1Status.operand, (Constant) op2Status.operand);
            assert foldResult != null;
            markConstant(inst.getResult(), foldResult);
        } else if (op1Status.operandStatus == Status.OperandStatus.multiDefined
                || op2Status.operandStatus == Status.OperandStatus.multiDefined)
            markMultiDefined(inst.getResult());
    }

    @Override
    public void visit(PhiInst inst) {
        Constant constant = null;
        for (Pair<Operand, BasicBlock> pair : inst.getBranch()) {
            if (!blockExecutable.contains(pair.getSecond()))
                continue;
            Status operandStatus = getStatus(pair.getFirst());
            if (operandStatus.operandStatus == Status.OperandStatus.multiDefined) {
                markMultiDefined(inst.getResult());
                return;
            } else if (operandStatus.operandStatus == Status.OperandStatus.constant) {
                if (constant != null) {
                    if (!constant.equals(pair.getFirst())) {
                        markMultiDefined(inst.getResult());
                        return;
                    }
                } else
                    constant = (Constant) operandStatus.operand;
            }
        }

        if (constant != null)
            markConstant(inst.getResult(), constant);
    }

    @Override
    public void visit(CallInst inst) {
        if (!inst.isVoidCall())
            markMultiDefined(inst.getResult());
    }

    private Constant foldConstant(IRInstruction inst, Constant lhs, Constant rhs) {
        assert inst instanceof BinaryOpInst || inst instanceof IcmpInst;
        Constant result;
        if (inst instanceof BinaryOpInst) {
            if (lhs instanceof ConstInt && rhs instanceof ConstInt) {
                long value;
                switch (((BinaryOpInst) inst).getOp()) {
                    case add:
                        value = ((ConstInt) lhs).getValue() + ((ConstInt) rhs).getValue();
                        break;
                    case sub:
                        value = ((ConstInt) lhs).getValue() - ((ConstInt) rhs).getValue();
                        break;
                    case mul:
                        value = ((ConstInt) lhs).getValue() * ((ConstInt) rhs).getValue();
                        break;
                    case sdiv:
                        if (((ConstInt) rhs).getValue() == 0)
                            return null;
                        value = ((ConstInt) lhs).getValue() / ((ConstInt) rhs).getValue();
                        break;
                    case srem:
                        if (((ConstInt) rhs).getValue() == 0)
                            return null;
                        value = ((ConstInt) lhs).getValue() % ((ConstInt) rhs).getValue();
                        break;
                    case shl:
                        value = ((ConstInt) lhs).getValue() << ((ConstInt) rhs).getValue();
                        break;
                    case ashr:
                        value = ((ConstInt) lhs).getValue() >> ((ConstInt) rhs).getValue();
                        break;
                    case and:
                        value = ((ConstInt) lhs).getValue() & ((ConstInt) rhs).getValue();
                        break;
                    case or:
                        value = ((ConstInt) lhs).getValue() | ((ConstInt) rhs).getValue();
                        break;
                    case xor:
                        value = ((ConstInt) lhs).getValue() ^ ((ConstInt) rhs).getValue();
                        break;
                    default:
                        throw new RuntimeException();
                }
                result = new ConstInt(IntegerType.BitWidth.int32, value);
            } else if (lhs instanceof ConstBool && rhs instanceof ConstBool) {
                boolean value;
                switch (((BinaryOpInst) inst).getOp()) {
                    case and:
                        value = ((ConstBool) lhs).getValue() & ((ConstBool) rhs).getValue();
                        break;
                    case or:
                        value = ((ConstBool) lhs).getValue() | ((ConstBool) rhs).getValue();
                        break;
                    case xor:
                        value = ((ConstBool) lhs).getValue() ^ ((ConstBool) rhs).getValue();
                        break;
                    default:
                        throw new RuntimeException("Invalid operator " +
                                ((BinaryOpInst) inst).getOp().name() +
                                " between "+ lhs.getType() + " and " + rhs.getType() + ".");
                }
                result = new ConstBool(value);
            } else {
                throw new RuntimeException("Invalid const comparison between "
                        + lhs.getType() + " and " + rhs.getType() + ".");
            }
        } else {
            // inst instanceof IcmpInst
            boolean value;
            if (lhs instanceof ConstInt && rhs instanceof ConstInt) {
                switch (((IcmpInst) inst).getOperator()) {
                    case eq:
                        value = ((ConstInt) lhs).getValue() == ((ConstInt) rhs).getValue();
                        break;
                    case ne:
                        value = ((ConstInt) lhs).getValue() != ((ConstInt) rhs).getValue();
                        break;
                    case sgt:
                        value = ((ConstInt) lhs).getValue() > ((ConstInt) rhs).getValue();
                        break;
                    case sge:
                        value = ((ConstInt) lhs).getValue() >= ((ConstInt) rhs).getValue();
                        break;
                    case slt:
                        value = ((ConstInt) lhs).getValue() < ((ConstInt) rhs).getValue();
                        break;
                    case sle:
                        value = ((ConstInt) lhs).getValue() <= ((ConstInt) rhs).getValue();
                        break;
                    default:
                        throw new RuntimeException();
                }
            } else if (lhs instanceof ConstBool && rhs instanceof ConstBool) {
                switch (((IcmpInst) inst).getOperator()) {
                    case eq:
                        value = ((ConstBool) lhs).getValue() == ((ConstBool) rhs).getValue();
                        break;
                    case ne:
                        value = ((ConstBool) lhs).getValue() != ((ConstBool) rhs).getValue();
                        break;
                    default:
                        throw new RuntimeException("Invalid operator " +
                                ((IcmpInst) inst).getOperator().name() +
                                " between "+ lhs.getType() + " and " + rhs.getType() + ".");
                }
            } else if (lhs instanceof ConstNull && rhs instanceof ConstNull) {
                switch (((IcmpInst) inst).getOperator()) {
                    case eq:
                        value = true;
                        break;
                    case ne:
                        value = false;
                        break;
                    default:
                        throw new RuntimeException("Invalid operator " +
                                ((IcmpInst) inst).getOperator().name() + " between const null.");
                }
            } else {
                throw new RuntimeException("Invalid const comparison between "
                        + lhs.getType() + " and " + rhs.getType() + ".");
            }
            result = new ConstBool(value);
        }
        return result;
    }

    private boolean replaceRegisterWithConstant(BasicBlock block) {
        boolean changed = false;
        IRInstruction ptr = block.getInstHead();
        while (ptr != null) {
            IRInstruction next = ptr.getInstNext();
            changed |= ptr.replaceResultWithConstant(this);
            ptr = next;
        }
        return changed;
    }
}
