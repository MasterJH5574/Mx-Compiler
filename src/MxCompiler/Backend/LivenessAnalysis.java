package MxCompiler.Backend;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Function;
import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Module;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LivenessAnalysis extends ASMPass {
    public LivenessAnalysis(Module module) {
        super(module);
    }

    @Override
    public void run() {
        for (Function function : module.getFunctionMap().values())
            computeLiveOutSet(function);
    }

    private void computeLiveOutSet(Function function) {
        ArrayList<BasicBlock> dfsOrder = function.getDFSOrder();
        for (BasicBlock block : dfsOrder)
            computeUEVarAndVarKill(block);

        for (int i = dfsOrder.size() - 1; i >= 0; i--) {
            BasicBlock block = dfsOrder.get(i);
            block.setLiveOut(new HashSet<>());
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = dfsOrder.size() - 1; i >= 0; i--) {
                BasicBlock block = dfsOrder.get(i);
                Set<VirtualRegister> liveOut = computeLiveOutSet(block);
                if (!block.getLiveOut().equals(liveOut)) {
                    block.setLiveOut(liveOut);
                    changed = true;
                }
            }
        }
    }

    private void computeUEVarAndVarKill(BasicBlock block) {
        Set<VirtualRegister> UEVar = new HashSet<>();
        Set<VirtualRegister> varKill = new HashSet<>();

        ASMInstruction ptr = block.getInstHead();
        while (ptr != null) {
            ptr.addToUEVarAndVarKill(UEVar, varKill);
            ptr = ptr.getNextInst();
        }

        block.setUEVar(UEVar);
        block.setVarKill(varKill);
    }

    private Set<VirtualRegister> computeLiveOutSet(BasicBlock block) {
        Set<VirtualRegister> liveOut = new HashSet<>();
        for (BasicBlock successor : block.getSuccessors()) {
            Set<VirtualRegister> intersection = new HashSet<>(successor.getLiveOut());
            intersection.removeAll(successor.getVarKill());

            Set<VirtualRegister> union = new HashSet<>(successor.getUEVar());
            union.addAll(intersection);

            liveOut.addAll(union);
        }
        return liveOut;
    }
}
