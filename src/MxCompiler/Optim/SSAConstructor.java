package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;

import java.util.*;

public class SSAConstructor extends Pass {
    private Map<BasicBlock, Map<AllocateInst, PhiInst>> phiInstMap;
    private Map<LoadInst, AllocateInst> useAlloca;
    private Map<StoreInst, AllocateInst> defAlloca;
    private Map<BasicBlock, Map<AllocateInst, Operand>> renameTable;
    private Set<BasicBlock> visit;

    public SSAConstructor(Module module) {
        super(module);
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values())
            constructSSA(function);
        return true;
    }

    private void constructSSA(Function function) {
        ArrayList<AllocateInst> allocaInst = function.getAllocaInstructions();
        phiInstMap = new HashMap<>();
        useAlloca = new HashMap<>();
        defAlloca = new HashMap<>();

        for (AllocateInst alloca : allocaInst) {
            ArrayList<StoreInst> defs = new ArrayList<>();
            for (IRInstruction useInst : alloca.getResult().getUse()) {
                assert useInst instanceof LoadInst || useInst instanceof StoreInst;
                if (useInst instanceof LoadInst)
                    useAlloca.put((LoadInst) useInst, alloca);
                else {
                    defs.add((StoreInst) useInst);
                    defAlloca.put((StoreInst) useInst, alloca);
                }
            }

            Queue<BasicBlock> queue = new LinkedList<>();
            HashSet<BasicBlock> visitSet = new HashSet<>();
            HashSet<BasicBlock> phiSet = new HashSet<>();
            for (StoreInst def : defs) {
                queue.offer(def.getBasicBlock());
                visitSet.add(def.getBasicBlock());
            }
            while (!queue.isEmpty()) {
                BasicBlock block = queue.poll();
                assert block != null;

                for (BasicBlock df : block.getDF()) {
                    if (!phiSet.contains(df)) {
                        addPhiInst(df, alloca);
                        phiSet.add(df);
                        if (!visitSet.contains(df)) {
                            queue.offer(df);
                            visitSet.add(df);
                        }
                    }
                }
            }

            alloca.removeFromBlock();
        }

        renameTable = new HashMap<>();
        visit = new HashSet<>();
        rename(function.getEntranceBlock(), null);
    }

    private void addPhiInst(BasicBlock block, AllocateInst alloca) {
        if (!phiInstMap.containsKey(block))
            phiInstMap.put(block, new HashMap<>());
        String name = alloca.getResult().getName().split("\\$")[0];
        phiInstMap.get(block).put(alloca, new PhiInst(block, new ArrayList<>(), new Register(alloca.getType(), name)));
    }

    private void rename(BasicBlock block, BasicBlock predecessor) {
        if (phiInstMap.containsKey(block)) {
            Map<AllocateInst, PhiInst> map = phiInstMap.get(block);
            for (AllocateInst alloca : map.keySet()) {
                PhiInst phiInst = map.get(alloca);
                Operand value;
                if (!renameTable.containsKey(predecessor)
                        || !renameTable.get(predecessor).containsKey(alloca)
                        || renameTable.get(predecessor).get(alloca) == null) {
                    value = alloca.getType().getDefaultValue();
                } else {
                    value = renameTable.get(predecessor).get(alloca);
                }
                phiInst.addBranch(value, predecessor);
            }
        }

        if (visit.contains(block))
            return;
        visit.add(block);

        if (phiInstMap.containsKey(block)) {
            Map<AllocateInst, PhiInst> map = phiInstMap.get(block);
            for (AllocateInst alloca : map.keySet()) {
                PhiInst phiInst = map.get(alloca);

                if (!renameTable.containsKey(block))
                    renameTable.put(block, new HashMap<>());
                renameTable.get(block).put(alloca, phiInst.getResult());
            }
        }

        ArrayList<IRInstruction> instructions = block.getInstructions();
        for (IRInstruction instruction : instructions) {
            if (instruction instanceof LoadInst && useAlloca.containsKey(instruction)) {
                AllocateInst alloca = useAlloca.get(instruction);
                assert renameTable.containsKey(block) && renameTable.get(block).containsKey(alloca);
                Operand value = renameTable.get(block).get(alloca);
                ((LoadInst) instruction).getResult().replaceUse(value);
                instruction.removeFromBlock();
            } else if (instruction instanceof StoreInst && defAlloca.containsKey(instruction)) {
                AllocateInst alloca = defAlloca.get(instruction);
                if (!renameTable.containsKey(block))
                    renameTable.put(block, new HashMap<>());
                if (!renameTable.get(block).containsKey(alloca))
                    renameTable.get(block).put(alloca, ((StoreInst) instruction).getValue());
                else
                    renameTable.get(block).replace(alloca, ((StoreInst) instruction).getValue());
            }
        }

        for (BasicBlock successor : block.getSuccessors())
            rename(successor, block);
    }
}
