package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.BranchInst;
import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Instruction.PhiInst;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.ConstBool;

import java.util.ArrayList;

public class CFGSimplifier extends Pass {
    public CFGSimplifier(Module module) {
        super(module);
        changed = false;
    }

    @Override
    public boolean run() {
        changed = false;
        for (Function function : module.getFunctionMap().values())
            changed |= functionSimplify(function);
        return changed;
    }

    private boolean functionSimplify(Function function) {
        boolean changed = false;
        while (true) {
            boolean loopChanged;
            loopChanged = removeRedundantBranch(function);
            loopChanged |= removeUnreachableBlock(function);
            loopChanged |= removePhiInstWithSingleBranch(function);
            if (loopChanged)
                changed = true;
            else
                break;
        }
        return changed;
    }

    private boolean removeRedundantBranch(Function function) {
        boolean changed = false;
        ArrayList<BasicBlock> dfsOrder = function.getDFSOrder();
        for (int i = dfsOrder.size() - 1; i >= 0; i--) {
            BasicBlock block = dfsOrder.get(i);
            if (block.getInstTail() instanceof BranchInst) {
                BranchInst branchInst = (BranchInst) block.getInstTail();
                if (branchInst.getThenBlock() == branchInst.getElseBlock()) {
                    branchInst.setUnconditionalBranch(branchInst.getThenBlock());
                    changed = true;
                } else if (branchInst.getCond() instanceof ConstBool) {
                    boolean cond = ((ConstBool) branchInst.getCond()).getValue();
                    BasicBlock thenBlock;
                    BasicBlock cutBlock;
                    if (cond) {
                        thenBlock = branchInst.getThenBlock();
                        cutBlock = branchInst.getElseBlock();
                    } else {
                        thenBlock = branchInst.getElseBlock();
                        cutBlock = branchInst.getThenBlock();
                    }
                    block.getSuccessors().remove(cutBlock);
                    cutBlock.getPredecessors().remove(block);
                    cutBlock.removePhiIncomingBlock(block);
                    branchInst.setUnconditionalBranch(thenBlock);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private boolean removeUnreachableBlock(Function function) {
        boolean changed = false;
        BasicBlock block = function.getEntranceBlock();
        ArrayList<BasicBlock> dfsOrder = function.getDFSOrder();
        while (block != null) {
            if (!dfsOrder.contains(block)) {
                block.removeFromFunction();
                changed = true;
            } else if (block.getPredecessors().size() == 1) {
                BasicBlock predecessor = block.getPredecessors().iterator().next();
                if (predecessor.getSuccessors().size() == 1) {
                    if (predecessor == block)
                        block.removeFromFunction();
                    else
                        predecessor.mergeBlock(block);
                    changed = true;
                }
            }
            block = block.getNext();
        }
        return changed;
    }

    private boolean removePhiInstWithSingleBranch(Function function) {
        boolean changed = false;
        ArrayList<BasicBlock> blocks = function.getBlocks();
        for (BasicBlock block : blocks) {
            IRInstruction ptr = block.getInstHead();
            while (ptr instanceof PhiInst) {
                IRInstruction next = ptr.getInstNext();
                if (((PhiInst) ptr).getBranch().size() == 1) {
                    assert block.getPredecessors().size() == 1;
                    ptr.getResult().replaceUse(((PhiInst) ptr).getBranch().iterator().next().getFirst());
                    ptr.removeFromBlock();
                    changed = true;
                }
                ptr = next;
            }
        }
        return changed;
    }
}
