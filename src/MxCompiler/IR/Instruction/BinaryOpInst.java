package MxCompiler.IR.Instruction;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRObject;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.IntegerType;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.Optim.Andersen;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.LoopOptim.LICM;
import MxCompiler.Optim.LoopOptim.LoopAnalysis;
import MxCompiler.Optim.SCCP;
import MxCompiler.Optim.SideEffectChecker;

import java.util.ArrayList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class BinaryOpInst extends IRInstruction {
    public enum BinaryOpName {
        add, sub, mul, sdiv, srem,          // Binary Operations
        shl, ashr, and, or, xor             // Bitwise Binary Operations
    }

    private BinaryOpName op;
    private Operand lhs;
    private Operand rhs;
    private Register result;

    public BinaryOpInst(BasicBlock basicBlock, BinaryOpName op, Operand lhs, Operand rhs, Register result) {
        super(basicBlock);
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
        this.result = result;

        assert lhs.getType().equals(result.getType());
        assert rhs.getType().equals(result.getType());
        assert !(result.getType() instanceof PointerType);
    }

    @Override
    public void successfullyAdd() {
        result.setDef(this);
        lhs.addUse(this);
        rhs.addUse(this);
    }

    public BinaryOpName getOp() {
        return op;
    }

    public Operand getLhs() {
        return lhs;
    }

    public Operand getRhs() {
        return rhs;
    }

    @Override
    public Register getResult() {
        return result;
    }

    public boolean isLogicalNot() {
        return op == BinaryOpName.xor
                && (lhs.equals(new ConstBool(true)) || rhs.equals(new ConstBool(true)));
    }

    public Operand getLogicalNotOperand() {
        if (lhs.equals(new ConstBool(true)))
            return rhs;
        else
            return lhs;
    }

    public boolean isIntegerNot() {
        return op == BinaryOpName.xor
                && (lhs.equals(new ConstInt(IntegerType.BitWidth.int32, -1))
                || rhs.equals(new ConstInt(IntegerType.BitWidth.int32, -1)));
    }

    public boolean isNegative() {
        return op == BinaryOpName.sub && lhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0));
    }

    public Operand getNegativeOperand() {
        return rhs;
    }

    public boolean shouldSwapOperands() {
        return lhs.getPrivilege() < rhs.getPrivilege();
    }

    public void swapOperands() {
        Operand tmp = lhs;
        lhs = rhs;
        rhs = tmp;
    }

    private void pushResultInQueue(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        for (IRInstruction instruction : result.getUse().keySet()) {
            if (!inQueue.contains(instruction)) {
                queue.offer(instruction);
                inQueue.add(instruction);
            }
        }
    }

    private boolean mergeConstantAndSwapOperands(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        if (this.shouldSwapOperands())
            this.swapOperands();
        int lhsPrivilege = lhs.getPrivilege();
        int rhsPrivilege = rhs.getPrivilege();
        if (lhsPrivilege == 2 && rhsPrivilege == 2) {
            // p = x + a, q = y + b, r = p + q
            //   ---->
            // u = x + y
            // r = u + (a + b)
            BinaryOpInst lhsDef = ((BinaryOpInst) ((Register) lhs).getDef());
            BinaryOpInst rhsDef = ((BinaryOpInst) ((Register) rhs).getDef());
            if (lhsDef.op != this.op || rhsDef.op != this.op
                    || lhs.getUse().size() != 1 || rhs.getUse().size() != 1)
                return false;

            if (lhsDef.shouldSwapOperands())
                lhsDef.swapOperands();
            if (rhsDef.shouldSwapOperands())
                rhsDef.swapOperands();
            if (lhsDef.getRhs() instanceof Constant && rhsDef.getRhs() instanceof Constant) {
                Register newResult = new Register(new IntegerType(IntegerType.BitWidth.int32), "adjust");
                this.getBasicBlock().getFunction().getSymbolTable().put(newResult.getName(), newResult);

                BinaryOpInst newInst = new BinaryOpInst(this.getBasicBlock(), this.op,
                        lhsDef.getLhs(), rhsDef.getLhs(), newResult);
                this.getBasicBlock().addInstructionPrev(this, newInst);

                replaceLhs(newResult);
                replaceRhs(calculateConstant(op, ((Constant) lhsDef.getRhs()), ((Constant) rhsDef.getRhs())));
                pushResultInQueue(queue, inQueue);

                queue.offer(newInst);
                inQueue.add(newInst);

                return true;
            }
        } else if (lhsPrivilege == 2 && rhsPrivilege == 0) {
            // y = x + a, z = y + b
            //   ---->
            // z = x + (a + b)
            BinaryOpInst lhsDef = ((BinaryOpInst) ((Register) lhs).getDef());
            if (lhsDef.op != this.op)
                return false;

            if (lhsDef.shouldSwapOperands())
                lhsDef.swapOperands();
            if (lhsDef.getRhs() instanceof Constant) {
                replaceLhs(lhsDef.getLhs());
                replaceRhs(calculateConstant(op, ((Constant) lhsDef.getRhs()), ((Constant) rhs)));
                pushResultInQueue(queue, inQueue);
                return true;
            }
        }
        return false;
    }

    private boolean combineAdd(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        boolean changed = mergeConstantAndSwapOperands(queue, inQueue);

        // y = x + x  --->  y = x << 1
        if (lhs == rhs) {
            op = BinaryOpName.shl;
            replaceRhs(new ConstInt(IntegerType.BitWidth.int32, 1));
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = x + 0  --->  x replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0))) {
            result.replaceUse(lhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // z = x + (-y)  --->  z = x - y
        if (rhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) rhs).getDef()).isNegative()) {
            op = BinaryOpName.sub;
            replaceRhs(((BinaryOpInst) ((Register) rhs).getDef()).getNegativeOperand());
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // z = (-x) + y  --->  z = y - x
        if (lhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) lhs).getDef()).isNegative()) {
            Operand newLhs = rhs;
            Operand newRhs = ((BinaryOpInst) ((Register) lhs).getDef()).getNegativeOperand();
            op = BinaryOpName.sub;
            replaceLhs(newLhs);
            replaceRhs(newRhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }

        // y = (a - b) + c: check a + c, c - b
        if (lhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) lhs).getDef()).op == BinaryOpName.sub) {
            Operand a = ((BinaryOpInst) ((Register) lhs).getDef()).lhs;
            Operand b = ((BinaryOpInst) ((Register) lhs).getDef()).rhs;
            // y = ((-c) - b) + c  --->  y = 0 - b
            if (a.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) a).getDef()).isNegative()
                    && rhs == ((BinaryOpInst) ((Register) a).getDef()).getNegativeOperand()) {
                Operand newLhs = new ConstInt(IntegerType.BitWidth.int32, 0);
                op = BinaryOpName.sub;
                replaceLhs(newLhs);
                replaceRhs(b);
                pushResultInQueue(queue, inQueue);
                return true;
            }
            // y = (a - b) + b  --->  a replace y
            if (b == rhs) {
                result.replaceUse(a);
                pushResultInQueue(queue, inQueue);
                return true;
            }
        }
        // y = a + (b - c): check a + b, a - c
        if (rhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) rhs).getDef()).op == BinaryOpName.sub) {
            Operand b = ((BinaryOpInst) ((Register) rhs).getDef()).lhs;
            Operand c = ((BinaryOpInst) ((Register) rhs).getDef()).rhs;
            // y = a + ((-a) - c)  --->  y = 0 - c
            if (b.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) b).getDef()).isNegative()
                    && lhs == ((BinaryOpInst) ((Register) b).getDef()).getNegativeOperand()) {
                Operand newLhs = new ConstInt(IntegerType.BitWidth.int32, 0);
                op = BinaryOpName.sub;
                replaceLhs(newLhs);
                replaceLhs(c);
                pushResultInQueue(queue, inQueue);
                return true;
            }
            // y = a + (b - a)  --->  b replace y
            if (c == lhs) {
                result.replaceUse(b);
                pushResultInQueue(queue, inQueue);
                return true;
            }
        }

        return changed;
    }

    private boolean combineSub(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        // y = x - x  --->  0 replace y
        if (lhs == rhs) {
            result.replaceUse(new ConstInt(IntegerType.BitWidth.int32, 0));
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = x - 0  --->  x replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0))) {
            result.replaceUse(lhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // z = (-x) - (-y)  --->  z = y - x
        if (lhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) lhs).getDef()).isNegative()
                && rhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) rhs).getDef()).isNegative()) {
            Operand newLhs = ((BinaryOpInst) ((Register) rhs).getDef()).getNegativeOperand();
            Operand newRhs = ((BinaryOpInst) ((Register) lhs).getDef()).getNegativeOperand();
            replaceLhs(newLhs);
            replaceRhs(newRhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // z = x - (-y)  --->  z = x + y
        if (rhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) rhs).getDef()).isNegative()) {
            op = BinaryOpName.add;
            replaceRhs(((BinaryOpInst) ((Register) rhs).getDef()).getNegativeOperand());
            pushResultInQueue(queue, inQueue);
            return true;
        }

        // y = 0 - (a - b)  --->  y = b - a
        if (this.isNegative() && this.getNegativeOperand().registerDefIsBinaryOpInst()
                && ((BinaryOpInst) ((Register) this.getNegativeOperand()).getDef()).op == BinaryOpName.sub) {
            Operand newLhs = ((BinaryOpInst) ((Register) this.getNegativeOperand()).getDef()).rhs;
            Operand newRhs = ((BinaryOpInst) ((Register) this.getNegativeOperand()).getDef()).lhs;
            replaceLhs(newLhs);
            replaceRhs(newRhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }

        // y = (a + b) - c: check a - c, b - c
        if (lhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) lhs).getDef()).op == BinaryOpName.add) {
            Operand a = ((BinaryOpInst) ((Register) lhs).getDef()).lhs;
            Operand b = ((BinaryOpInst) ((Register) lhs).getDef()).rhs;
            // y = (c + b) - c  --->  b replace y
            if (a == rhs) {
                result.replaceUse(b);
                pushResultInQueue(queue, inQueue);
                return true;
            }
            // y = (a + c) - c  --->  a replace y
            if (b == rhs) {
                result.replaceUse(a);
                pushResultInQueue(queue, inQueue);
                return true;
            }
        }
        // y = (a - b) - c: check a - c
        if (lhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) lhs).getDef()).op == BinaryOpName.sub) {
            Operand a = ((BinaryOpInst) ((Register) lhs).getDef()).lhs;
            Operand b = ((BinaryOpInst) ((Register) lhs).getDef()).rhs;
            // y = (c - b) - c  --->  y = 0 - b
            if (a == rhs) {
                Operand newLhs = new ConstInt(IntegerType.BitWidth.int32, 0);
                replaceLhs(newLhs);
                replaceRhs(b);
                pushResultInQueue(queue, inQueue);
                return true;
            }
        }
        // y = a - (b + c): check a - b, a - c
        if (rhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) rhs).getDef()).op == BinaryOpName.add) {
            Operand b = ((BinaryOpInst) ((Register) rhs).getDef()).lhs;
            Operand c = ((BinaryOpInst) ((Register) rhs).getDef()).rhs;
            // y = a - (a + c)  --->  y = 0 - c
            if (lhs == b) {
                Operand newLhs = new ConstInt(IntegerType.BitWidth.int32, 0);
                replaceLhs(newLhs);
                replaceRhs(c);
                pushResultInQueue(queue, inQueue);
                return true;
            }
            // y = a - (b + a)  --->  y = 0 - b
            if (lhs == c) {
                Operand newLhs = new ConstInt(IntegerType.BitWidth.int32, 0);
                replaceLhs(newLhs);
                replaceRhs(b);
                pushResultInQueue(queue, inQueue);
                return true;
            }
        }
        // y = a - (b - c): check a - b
        if (rhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) rhs).getDef()).op == BinaryOpName.sub) {
            Operand b = ((BinaryOpInst) ((Register) rhs).getDef()).lhs;
            Operand c = ((BinaryOpInst) ((Register) rhs).getDef()).rhs;
            // y = a - (a - c)  --->  c replace y
            if (lhs == b) {
                result.replaceUse(c);
                pushResultInQueue(queue, inQueue);
                return true;
            }
        }

        return false;
    }

    private boolean combineMul(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        boolean changed = mergeConstantAndSwapOperands(queue, inQueue);

        // y = x * 0  --->  0 replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0))) {
            result.replaceUse(new ConstInt(IntegerType.BitWidth.int32, 0));
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = x * 1  ---> x replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, 1))) {
            result.replaceUse(lhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = x * (-1)  --->  y = 0 - x
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, -1))) {
            Operand newLhs = new ConstInt(IntegerType.BitWidth.int32, 0);
            Operand newRhs = lhs;
            op = BinaryOpName.mul;
            replaceLhs(newLhs);
            replaceRhs(newRhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }

        if (!(rhs instanceof ConstInt))
            return changed;
        int power = ((ConstInt) rhs).getPowerOfTwo();

        if (power > 0) {
            // y = x * (2^k)  --->  y = x << k
            op = BinaryOpName.shl;
            replaceRhs(new ConstInt(IntegerType.BitWidth.int32, power));
            pushResultInQueue(queue, inQueue);
            return true;
        } else if (power < 0) {
            // y = x * (-(2^k))  --->  r = x << k, y = 0 - r
            Register shlResult = new Register(result.getType(), "shiftLeft");
            this.getBasicBlock().getFunction().getSymbolTable().put(shlResult.getName(), shlResult);

            BinaryOpInst shiftLeft = new BinaryOpInst(this.getBasicBlock(),
                    BinaryOpName.shl, lhs, new ConstInt(IntegerType.BitWidth.int32, -power), shlResult);
            this.getBasicBlock().addInstructionPrev(this, shiftLeft);
            queue.offer(shiftLeft);
            inQueue.add(shiftLeft);

            op = BinaryOpName.sub;
            replaceLhs(new ConstInt(IntegerType.BitWidth.int32, 0));
            replaceRhs(shlResult);
            pushResultInQueue(queue, inQueue);
            return true;
        }

        return changed;
    }

    private boolean combineSdiv(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        // y = 0 / x  --->  0 replace y
        if (lhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0))) {
            result.replaceUse(new ConstInt(IntegerType.BitWidth.int32, 0));
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = x / 1  --->  x replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, 1))) {
            result.replaceUse(lhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = x / (-1)  --->  y = 0 - x
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, -1))) {
            Operand newLhs = new ConstInt(IntegerType.BitWidth.int32, 0);
            Operand newRhs = lhs;
            op = BinaryOpName.sub;
            replaceLhs(newLhs);
            replaceRhs(newRhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }

        // Can y = x / (2^k) be optimized to y = x >> k?
        // No!!!!
        // If x < 0, ashr will lead to wrong answer.
        // What a pity!
        return false;
    }

    private boolean combineSrem(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        // y = 0 % x  --->  0 replace y
        // y = x % 1  --->  0 replace y
        // y = x % -1  --->  0 replace y
        if (lhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0))
                || rhs.equals(new ConstInt(IntegerType.BitWidth.int32, 1))
                || rhs.equals(new ConstInt(IntegerType.BitWidth.int32, -1))) {
            result.replaceUse(new ConstInt(IntegerType.BitWidth.int32, 0));
            pushResultInQueue(queue, inQueue);
            return true;
        }

        return false;
    }

    private boolean combineShl(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        // y = 0 << x  --->  0 replace y
        if (lhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0))) {
            result.replaceUse(new ConstInt(IntegerType.BitWidth.int32, 0));
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = x << 0  --->  x replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0))) {
            result.replaceUse(lhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = (x << a) << b  --->  y = x << (a + b)
        if (lhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) lhs).getDef()).op == BinaryOpName.shl) {
            Operand a = ((BinaryOpInst) ((Register) lhs).getDef()).rhs;
            if (a instanceof ConstInt && rhs instanceof ConstInt) {
                Operand newLhs = ((BinaryOpInst) ((Register) lhs).getDef()).lhs;
                Operand newRhs = calculateConstant(BinaryOpName.add, ((ConstInt) a), ((ConstInt) rhs));
                replaceLhs(newLhs);
                replaceRhs(newRhs);
                pushResultInQueue(queue, inQueue);
                return true;
            }
        }
        // y = (x * a) << b  --->  y = x * (a << b)
        if (lhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) lhs).getDef()).op == BinaryOpName.mul) {
            Operand a = ((BinaryOpInst) ((Register) lhs).getDef()).rhs;
            if (a instanceof ConstInt && rhs instanceof ConstInt) {
                Operand newLhs = ((BinaryOpInst) ((Register) lhs).getDef()).lhs;
                Operand newRhs = calculateConstant(BinaryOpName.shl, ((ConstInt) a), ((ConstInt) rhs));
                replaceLhs(newLhs);
                replaceRhs(newRhs);
                pushResultInQueue(queue, inQueue);
                return true;
            }
        }

        return false;
    }

    private boolean combineAshr(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        // y = 0 >> x  --->  0 replace y
        if (lhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0))) {
            result.replaceUse(new ConstInt(IntegerType.BitWidth.int32, 0));
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = x >> 0  --->  x replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0))) {
            result.replaceUse(lhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = (x >> a) >> b  --->  y = x >> (a + b)
        if (lhs.registerDefIsBinaryOpInst() && ((BinaryOpInst) ((Register) lhs).getDef()).op == BinaryOpName.ashr) {
            Operand a = ((BinaryOpInst) ((Register) lhs).getDef()).rhs;
            if (a instanceof ConstInt && rhs instanceof ConstInt) {
                Operand newLhs = ((BinaryOpInst) ((Register) lhs).getDef()).lhs;
                Operand newRhs = calculateConstant(BinaryOpName.add, ((ConstInt) a), ((ConstInt) rhs));
                replaceLhs(newLhs);
                replaceRhs(newRhs);
                pushResultInQueue(queue, inQueue);
                return true;
            }
        }

        return false;
    }

    private boolean combineAnd(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        if (((IntegerType) result.getType()).getBitWidth() == IntegerType.BitWidth.int1)
            return false;

        boolean changed = mergeConstantAndSwapOperands(queue, inQueue);

        // y = x & 0  --->  0 replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0))) {
            result.replaceUse(new ConstInt(IntegerType.BitWidth.int32, 0));
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = x & (-1)  --->  x replace y
        // y = x & x  --->  x replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, -1)) || lhs == rhs) {
            result.replaceUse(lhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }

        return changed;
    }

    private boolean combineOr(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        if (((IntegerType) result.getType()).getBitWidth() == IntegerType.BitWidth.int1)
            return false;

        boolean changed = mergeConstantAndSwapOperands(queue, inQueue);

        // y = x || 0  --->  x replace y
        // y = x || x  --->  x replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0)) || lhs == rhs) {
            result.replaceUse(lhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = x || (-1)  ---> -1 replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, -1))) {
            result.replaceUse(new ConstInt(IntegerType.BitWidth.int32, -1));
            pushResultInQueue(queue, inQueue);
            return true;
        }

        return changed;
    }

    private boolean combineXor(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        if (((IntegerType) result.getType()).getBitWidth() == IntegerType.BitWidth.int1)
            return false;

        boolean changed = mergeConstantAndSwapOperands(queue, inQueue);

        // y = x ^ 0  --->  x replace y
        if (rhs.equals(new ConstInt(IntegerType.BitWidth.int32, 0))) {
            result.replaceUse(lhs);
            pushResultInQueue(queue, inQueue);
            return true;
        }
        // y = x ^ x  --->  0 replace y
        if (lhs == rhs) {
            result.replaceUse(new ConstInt(IntegerType.BitWidth.int32, 0));
            pushResultInQueue(queue, inQueue);
            return true;
        }

        return changed;
    }

    private Constant calculateConstant(BinaryOpName op, Constant lhs, Constant rhs) {
        if (op == BinaryOpName.add) {
            assert lhs instanceof ConstInt;
            assert rhs instanceof ConstInt;
            return new ConstInt(IntegerType.BitWidth.int32,
                    ((ConstInt) lhs).getValue() + ((ConstInt) rhs).getValue());
        } else if (op == BinaryOpName.mul) {
            assert lhs instanceof ConstInt;
            assert rhs instanceof ConstInt;
            return new ConstInt(IntegerType.BitWidth.int32,
                    ((ConstInt) lhs).getValue() + ((ConstInt) rhs).getValue());
        } else if (op == BinaryOpName.shl) {
            assert lhs instanceof ConstInt;
            assert rhs instanceof ConstInt;
            return new ConstInt(IntegerType.BitWidth.int32,
                    ((ConstInt) lhs).getValue() << ((ConstInt) rhs).getValue());
        }
        return null;
    }

    private void replaceLhs(Operand newLhs) {
        lhs.removeUse(this);
        lhs = newLhs;
        lhs.addUse(this);
    }

    private void replaceRhs(Operand newRhs) {
        rhs.removeUse(this);
        rhs = newRhs;
        rhs.addUse(this);
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        if (lhs == oldUse) {
            lhs.removeUse(this);
            lhs = (Operand) newUse;
            lhs.addUse(this);
        }
        if (rhs == oldUse) {
            rhs.removeUse(this);
            rhs = (Operand) newUse;
            rhs.addUse(this);
        }
    }

    @Override
    public void removeFromBlock() {
        lhs.removeUse(this);
        rhs.removeUse(this);
        super.removeFromBlock();
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        lhs.markBaseAsLive(live, queue);
        rhs.markBaseAsLive(live, queue);
    }

    @Override
    public boolean replaceResultWithConstant(SCCP sccp) {
        SCCP.Status status = sccp.getStatus(result);
        if (status.getOperandStatus() == SCCP.Status.OperandStatus.constant) {
            result.replaceUse(status.getOperand());
            this.removeFromBlock();
            return true;
        } else
            return false;
    }

    @Override
    public CSE.Expression convertToExpression() {
        String instructionName = op.name();
        ArrayList<String> operands = new ArrayList<>();
        operands.add(lhs.toString());
        operands.add(rhs.toString());
        return new CSE.Expression(instructionName, operands);
    }

    @Override
    public void clonedUseReplace(Map<BasicBlock, BasicBlock> blockMap, Map<Operand, Operand> operandMap) {
        if (lhs instanceof Parameter || lhs instanceof Register) {
            assert operandMap.containsKey(lhs);
            lhs = operandMap.get(lhs);
        }
        if (rhs instanceof Parameter || rhs instanceof Register) {
            assert operandMap.containsKey(rhs);
            rhs = operandMap.get(rhs);
        }
        lhs.addUse(this);
        rhs.addUse(this);
    }

    @Override
    public void addConstraintsForAndersen(Map<Operand, Andersen.Node> nodeMap, Set<Andersen.Node> nodes) {
        // Do nothing.
    }

    @Override
    public boolean updateResultScope(Map<Operand, SideEffectChecker.Scope> scopeMap,
                                     Map<Function, SideEffectChecker.Scope> returnValueScope) {
        if (scopeMap.get(result) != SideEffectChecker.Scope.local) {
            scopeMap.replace(result, SideEffectChecker.Scope.local);
            return true;
        } else
            return false;
    }

    @Override
    public boolean checkLoopInvariant(LoopAnalysis.LoopNode loop, LICM licm) {
        if (licm.isLoopInvariant(result, loop))
            return false;
        if (op == BinaryOpName.sdiv && !(rhs instanceof Constant)) {
            BasicBlock block = this.getBasicBlock();
            Set<BasicBlock> exitBlocks = loop.getExitBlocks();
            for (BasicBlock exit : exitBlocks) {
                if (!block.dominate(exit))
                    return false;
            }
        }

        if (licm.isLoopInvariant(lhs, loop) && licm.isLoopInvariant(rhs, loop)) {
            licm.markLoopInvariant(result);
            return true;
        }
        return false;
    }

    @Override
    public boolean combineInst(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        if (op == BinaryOpName.add)
            return combineAdd(queue, inQueue);
        else if (op == BinaryOpName.sub)
            return combineSub(queue, inQueue);
        else if (op == BinaryOpName.mul)
            return combineMul(queue, inQueue);
        else if (op == BinaryOpName.sdiv)
            return combineSdiv(queue, inQueue);
        else if (op == BinaryOpName.srem)
            return combineSrem(queue, inQueue);
        else if (op == BinaryOpName.shl)
            return combineShl(queue, inQueue);
        else if (op == BinaryOpName.ashr)
            return combineAshr(queue, inQueue);
        else if (op == BinaryOpName.and)
            return combineAnd(queue, inQueue);
        else if (op == BinaryOpName.or)
            return combineOr(queue, inQueue);
        else
            return combineXor(queue, inQueue);
    }

    @Override
    public boolean canBeHoisted(LoopAnalysis.LoopNode loop) {
        return loop.defOutOfLoop(lhs) && loop.defOutOfLoop(rhs);
    }

    @Override
    public String toString() {
        return result.toString() + " = " +
                op.name() + " " + result.getType().toString() + " " + lhs.toString() + ", " + rhs.toString();
    }

    @Override
    public Object clone() {
        BinaryOpInst binaryOpInst = (BinaryOpInst) super.clone();
        binaryOpInst.op = this.op;
        binaryOpInst.lhs = this.lhs;
        binaryOpInst.rhs = this.rhs;
        binaryOpInst.result = (Register) this.result.clone();

        binaryOpInst.result.setDef(binaryOpInst);
        return binaryOpInst;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
