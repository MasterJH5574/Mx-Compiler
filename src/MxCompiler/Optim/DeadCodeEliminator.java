package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Module;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


public class DeadCodeEliminator extends Pass {
    public DeadCodeEliminator(Module module) {
        super(module);
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional())
                return false;
        }

        changed = false;
        while (true) {
            for (Function function : module.getFunctionMap().values())
                checkSideEffect(function);

            boolean loopChanged = false;
            for (Function function : module.getFunctionMap().values())
                loopChanged |= deadCodeElimination(function);
            if (loopChanged)
                changed = true;
            else
                break;
        }
        return changed;
    }

    private void checkSideEffect(Function function) {
        BasicBlock ptr = function.getEntranceBlock();
        while (ptr != null) {
            if (hasSideEffect(ptr)) {
                function.setSideEffect(true);
                return;
            }
            ptr = ptr.getNext();
        }
        function.setSideEffect(false);
    }

    private boolean hasSideEffect(BasicBlock basicBlock) {
        IRInstruction ptr = basicBlock.getInstHead();
        while (ptr != null) {
            if (ptr instanceof StoreInst)
                return true;
            if (ptr instanceof CallInst) {
                if (((CallInst) ptr).getFunction().hasSideEffect())
                    return true;
            }
            ptr = ptr.getInstNext();
        }
        return false;
    }

    private boolean deadCodeElimination(Function function) {
        if (function.isNotFunctional())
            return false;
        Set<IRInstruction> live = new HashSet<>();
        Queue<IRInstruction> queue = new LinkedList<>();
        for (BasicBlock block : function.getBlocks())
            addLiveInstructions(block, live, queue);

        while (!queue.isEmpty()) {
            IRInstruction instruction = queue.poll();
            instruction.markUseAsLive(live, queue);
            for (BasicBlock predecessor : instruction.getBasicBlock().getPredecessors()) {
                assert predecessor.getInstTail() instanceof BranchInst;
                if (!live.contains(predecessor.getInstTail())) {
                    live.add(predecessor.getInstTail());
                    queue.offer(predecessor.getInstTail());
                }
            }
        }

        boolean changed = false;
        for (BasicBlock block : function.getBlocks())
            changed |= removeDeadInstructions(block, live);
        return changed;
    }

    private void addLiveInstructions(BasicBlock block, Set<IRInstruction> live, Queue<IRInstruction> queue) {
        IRInstruction ptr = block.getInstHead();
        while (ptr != null) {
            if (ptr instanceof StoreInst) {
                live.add(ptr);
                queue.offer(ptr);
            } else if (ptr instanceof CallInst) {
                if (((CallInst) ptr).getFunction().hasSideEffect()) {
                    live.add(ptr);
                    queue.offer(ptr);
                }
            } else if (ptr instanceof ReturnInst) {
                live.add(ptr);
                queue.offer(ptr);
            }
            ptr = ptr.getInstNext();
        }
    }

    private boolean removeDeadInstructions(BasicBlock block, Set<IRInstruction> live) {
        IRInstruction ptr = block.getInstHead();
        boolean changed = false;
        while (ptr != null) {
            if (!live.contains(ptr)) {
                ptr.removeFromBlock();
                changed = true;
            }
            ptr = ptr.getInstNext();
        }
        return changed;
    }
}
