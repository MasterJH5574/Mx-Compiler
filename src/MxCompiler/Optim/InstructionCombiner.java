package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Module;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class InstructionCombiner extends Pass {
    private Queue<IRInstruction> queue;
    private Set<IRInstruction> inQueue;

    public InstructionCombiner(Module module) {
        super(module);
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional())
                return false;
        }

        queue = new LinkedList<>();
        inQueue = new HashSet<>();
        addInstructions();

        boolean changed = false;
        while (!queue.isEmpty()) {
            IRInstruction instruction = queue.poll();
            inQueue.remove(instruction);
            changed |= instruction.combineInst(queue, inQueue);
        }
        return changed;
    }

    private void addInstructions() {
        for (Function function : module.getFunctionMap().values()) {
            for (BasicBlock block : function.getBlocks()) {
                IRInstruction ptr = block.getInstHead();
                while (ptr != null) {
                    if (ptr instanceof BinaryOpInst || ptr instanceof BitCastToInst
                            || ptr instanceof BranchInst || ptr instanceof IcmpInst
                            || ptr instanceof GetElementPtrInst) {
                        queue.offer(ptr);
                        inQueue.add(ptr);
                    }
                    ptr = ptr.getInstNext();
                }
            }
        }
    }
}
