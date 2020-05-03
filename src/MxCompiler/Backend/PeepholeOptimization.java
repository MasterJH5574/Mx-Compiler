package MxCompiler.Backend;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Function;
import MxCompiler.RISCV.Instruction.JumpInst;
import MxCompiler.RISCV.Module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class PeepholeOptimization extends ASMPass {
    public PeepholeOptimization(Module module) {
        super(module);
    }

    @Override
    public void run() {
        for (Function function : module.getFunctionMap().values())
            peephole(function);
    }

    private void peephole(Function function) {
        rearrangeBlocks(function);

    }

    private void rearrangeBlocks(Function function) {
        ArrayList<BasicBlock> dfsOrder = function.getDFSOrder();
        Set<BasicBlock> positionFixed = new HashSet<>();

        for (BasicBlock block : dfsOrder) {
            if (positionFixed.contains(block))
                continue;
            BasicBlock curBlock = block;
            BasicBlock lastBlock = null;
            while (true) {
                positionFixed.add(curBlock);
                if (lastBlock != null) {
                    function.splitBlockFromFunction(curBlock);
                    function.addBasicBlockNext(lastBlock, curBlock);
                    lastBlock.removeTailJump();
                }
                if (!(curBlock.getInstTail() instanceof JumpInst)
                        || positionFixed.contains(((JumpInst) curBlock.getInstTail()).getDest()))
                    break;
                lastBlock = curBlock;
                curBlock = ((JumpInst) curBlock.getInstTail()).getDest();
            }
        }
    }
}
