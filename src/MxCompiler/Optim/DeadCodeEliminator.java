package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Module;
import MxCompiler.Optim.LoopOptim.LoopAnalysis;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


public class DeadCodeEliminator extends Pass {
    private SideEffectChecker sideEffectChecker;
    private LoopAnalysis loopAnalysis;

    public DeadCodeEliminator(Module module, SideEffectChecker sideEffectChecker, LoopAnalysis loopAnalysis) {
        super(module);
        this.sideEffectChecker = sideEffectChecker;
        this.loopAnalysis = loopAnalysis;
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional())
                return false;
        }

        changed = false;
        sideEffectChecker.setIgnoreIO(false);
        sideEffectChecker.setIgnoreLoad(true);
        sideEffectChecker.run();

        for (Function function : module.getFunctionMap().values())
            changed |= deadCodeElimination(function);
        return changed;
    }

    private boolean deadCodeElimination(Function function) {
        Set<IRInstruction> live = new HashSet<>();
        Queue<IRInstruction> queue = new LinkedList<>();
        for (BasicBlock block : function.getBlocks())
            addLiveInstructions(block, live, queue);

        while (!queue.isEmpty()) {
            IRInstruction instruction = queue.poll();
            instruction.markUseAsLive(live, queue);
            for (BasicBlock block : instruction.getBasicBlock().getPostDF()) {
                assert block.getInstTail() instanceof BranchInst;
                if (!live.contains(block.getInstTail())) {
                    live.add(block.getInstTail());
                    queue.offer(block.getInstTail());
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
                if (!live.contains(ptr.getBasicBlock().getInstTail())) {
                    live.add(ptr.getBasicBlock().getInstTail());
                    queue.offer(ptr.getBasicBlock().getInstTail());
                }
            } else if (ptr instanceof CallInst) {
                if (sideEffectChecker.hasSideEffect(((CallInst) ptr).getFunction())) {
                    live.add(ptr);
                    queue.offer(ptr);
                    if (!live.contains(ptr.getBasicBlock().getInstTail())) {
                        live.add(ptr.getBasicBlock().getInstTail());
                        queue.offer(ptr.getBasicBlock().getInstTail());
                    }
                }
            } else if (ptr instanceof ReturnInst) {
                live.add(ptr);
                queue.offer(ptr);
                if (!live.contains(ptr.getBasicBlock().getInstTail())) {
                    live.add(ptr.getBasicBlock().getInstTail());
                    queue.offer(ptr.getBasicBlock().getInstTail());
                }
            }
            ptr = ptr.getInstNext();
        }
    }

    private boolean removeDeadInstructions(BasicBlock block, Set<IRInstruction> live) {
        IRInstruction ptr = block.getInstHead();
        boolean changed = false;
        while (ptr != null) {
            if (!live.contains(ptr))
                changed |= ptr.dceRemoveFromBlock(loopAnalysis);
            ptr = ptr.getInstNext();
        }
        return changed;
    }
}
