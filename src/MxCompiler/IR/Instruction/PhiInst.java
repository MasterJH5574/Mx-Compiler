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
import MxCompiler.Optim.Andersen;
import MxCompiler.Optim.CSE;
import MxCompiler.Optim.LoopOptim.LICM;
import MxCompiler.Optim.LoopOptim.LoopAnalysis;
import MxCompiler.Optim.SCCP;
import MxCompiler.Optim.SideEffectChecker;
import MxCompiler.Utilities.Pair;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class PhiInst extends IRInstruction {
    private Set<Pair<Operand, BasicBlock>> branch;
    private Register result;

    public PhiInst(BasicBlock basicBlock, Set<Pair<Operand, BasicBlock>> branch, Register result) {
        super(basicBlock);
        this.branch = branch;
        this.result = result;

        for (Pair<Operand, BasicBlock> pair : branch) {
            assert pair.getFirst().getType().equals(result.getType())
                    || (pair.getFirst() instanceof ConstNull && result.getType() instanceof PointerType);
        }
    }

    @Override
    public void successfullyAdd() {
        for (Pair<Operand, BasicBlock> pair : branch) {
            pair.getFirst().addUse(this);
            pair.getSecond().addUse(this);
        }
        result.setDef(this);
    }

    public Set<Pair<Operand, BasicBlock>> getBranch() {
        return branch;
    }

    public void addBranch(Operand operand, BasicBlock block) {
        branch.add(new Pair<>(operand, block));
        operand.addUse(this);
        block.addUse(this);
    }

    public void removeIncomingBlock(BasicBlock block) {
        Pair<Operand, BasicBlock> pair = null;
        for (Pair<Operand, BasicBlock> pairInBranch : branch) {
            if (pairInBranch.getSecond() == block) {
                pair = pairInBranch;
                break;
            }
        }
        assert pair != null;
        removeIncomingBranch(pair);
    }

    public void removeIncomingBranch(Pair<Operand, BasicBlock> pair) {
        assert pair != null;
        pair.getFirst().removeUse(this);
        pair.getSecond().removeUse(this);
        branch.remove(pair);
    }

    @Override
    public Register getResult() {
        return result;
    }

    @Override
    public void replaceUse(IRObject oldUse, IRObject newUse) {
        for (Pair<Operand, BasicBlock> pair : branch) {
            if (pair.getFirst() == oldUse) {
                pair.getFirst().removeUse(this);
                pair.setFirst((Operand) newUse);
                pair.getFirst().addUse(this);
            } else if (pair.getSecond() == oldUse) {
                pair.getSecond().removeUse(this);
                pair.setSecond((BasicBlock) newUse);
                pair.getSecond().addUse(this);
            }
        }
    }

    @Override
    public void removeFromBlock() {
        for (Pair<Operand, BasicBlock> pair : branch) {
            pair.getFirst().removeUse(this);
            pair.getSecond().removeUse(this);
        }
        super.removeFromBlock();
    }

    @Override
    public void markUseAsLive(Set<IRInstruction> live, Queue<IRInstruction> queue) {
        for (Pair<Operand, BasicBlock> pair : branch) {
            pair.getFirst().markBaseAsLive(live, queue);
            if (pair.getSecond().isNotExitBlock() && !live.contains(pair.getSecond().getInstTail())) {
                live.add(pair.getSecond().getInstTail());
                queue.offer(pair.getSecond().getInstTail());
            }
        }
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
        throw new RuntimeException("Convert phi instruction to expression");
    }

    @Override
    public void clonedUseReplace(Map<BasicBlock, BasicBlock> blockMap, Map<Operand, Operand> operandMap) {
        Set<Pair<Operand, BasicBlock>> newBranch = new LinkedHashSet<>();
        for (Pair<Operand, BasicBlock> pair : branch) {
            Operand operand;
            BasicBlock block;
            if (pair.getFirst() instanceof Parameter || pair.getFirst() instanceof Register) {
                assert operandMap.containsKey(pair.getFirst());
                operand = operandMap.get(pair.getFirst());
            } else
                operand = pair.getFirst();
            operand.addUse(this);

            assert blockMap.containsKey(pair.getSecond());
            block = blockMap.get(pair.getSecond());
            block.addUse(this);

            newBranch.add(new Pair<>(operand, block));
        }
        this.branch = newBranch;
    }

    @Override
    public void addConstraintsForAndersen(Map<Operand, Andersen.Node> nodeMap, Set<Andersen.Node> nodes) {
        if (!(result.getType() instanceof PointerType))
            return;
        assert nodeMap.containsKey(result);
        for (Pair<Operand, BasicBlock> pair : branch) {
            Operand operand = pair.getFirst();
            if (!(operand instanceof ConstNull)) {
                assert nodeMap.containsKey(operand);
                nodeMap.get(operand).getInclusiveEdge().add(nodeMap.get(result));
            }
        }
    }

    @Override
    public boolean updateResultScope(Map<Operand, SideEffectChecker.Scope> scopeMap,
                                     Map<Function, SideEffectChecker.Scope> returnValueScope) {
        if (SideEffectChecker.getOperandScope(result) == SideEffectChecker.Scope.local) {
            if (scopeMap.get(result) != SideEffectChecker.Scope.local) {
                scopeMap.replace(result, SideEffectChecker.Scope.local);
                return true;
            } else
                return false;
        }

        for (Pair<Operand, BasicBlock> pair : branch) {
            if (pair.getFirst() instanceof ConstNull)
                continue;
            SideEffectChecker.Scope scope = scopeMap.get(pair.getFirst());
            if (scope == SideEffectChecker.Scope.undefined)
                continue;
            if (scope == SideEffectChecker.Scope.outer) {
                if (scopeMap.get(result) != SideEffectChecker.Scope.outer) {
                    scopeMap.replace(result, SideEffectChecker.Scope.outer);
                    return true;
                } else
                    return false;
            }
        }
        if (scopeMap.get(result) != SideEffectChecker.Scope.local) {
            scopeMap.replace(result, SideEffectChecker.Scope.local);
            return true;
        } else
            return false;
    }

    @Override
    public boolean checkLoopInvariant(LoopAnalysis.LoopNode loop, LICM licm) {
        return false;
    }

    @Override
    public boolean canBeHoisted(LoopAnalysis.LoopNode loop) {
        return false;
    }

    @Override
    public boolean combineInst(Queue<IRInstruction> queue, Set<IRInstruction> inQueue) {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append(result.toString()).append(" = phi ").append(result.getType().toString()).append(" ");
        int size = branch.size();
        int cnt = 0;
        for (Pair<Operand, BasicBlock> pair : branch) {
            string.append("[ ").append(pair.getFirst().toString()).
                    append(", ").append(pair.getSecond().toString()).append(" ]");
            if (++cnt != size)
                string.append(", ");
        }
        return string.toString();
    }

    @Override
    public Object clone() {
        PhiInst phiInst = (PhiInst) super.clone();
        phiInst.branch = new LinkedHashSet<>(this.branch);
        phiInst.result = (Register) this.result.clone();

        phiInst.result.setDef(phiInst);
        return phiInst;
    }

    @Override
    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
