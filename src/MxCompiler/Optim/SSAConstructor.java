package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;

import java.util.*;

public class SSAConstructor extends Pass {
    private ArrayList<AllocateInst> allocaInst;
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
        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional())
                return false;
        }
        for (Function function : module.getFunctionMap().values())
            constructSSA(function);
        return true;
    }

    private void constructSSA(Function function) {
        allocaInst = function.getAllocaInstructions();
        phiInstMap = new HashMap<>();
        useAlloca = new HashMap<>();
        defAlloca = new HashMap<>();
        renameTable = new HashMap<>();

        for (BasicBlock block : function.getBlocks()) {
            phiInstMap.put(block, new HashMap<>());
            renameTable.put(block, new HashMap<>());
        }

        for (AllocateInst alloca : allocaInst) {
            ArrayList<StoreInst> defs = new ArrayList<>();
            for (IRInstruction useInst : alloca.getResult().getUse().keySet()) {
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

        // Remove some redundant loads first to insure no exception occurs during renaming.
        // A very simple dead code elimination which only removes loads.
        // Why? Avoid using a variable before any of its definition.
        loadInstElimination(function);

        visit = new HashSet<>();
        rename(function.getEntranceBlock(), null);
    }

    private void addPhiInst(BasicBlock block, AllocateInst alloca) {
        String name = alloca.getResult().getName().split("\\$")[0];
        Register result = new Register(alloca.getType(), name);
        phiInstMap.get(block).put(alloca, new PhiInst(block, new LinkedHashSet<>(), result));
        block.getFunction().getSymbolTable().put(result.getName(), result);
    }

    private void loadInstElimination(Function function) {
        for (BasicBlock block : function.getBlocks()) {
            ArrayList<IRInstruction> instructions = block.getInstructions();
            for (IRInstruction instruction : instructions) {
                if (instruction instanceof LoadInst && instruction.getResult().getUse().isEmpty())
                    instruction.removeFromBlock();
            }
        }
    }

    private void rename(BasicBlock block, BasicBlock predecessor) {
        Map<AllocateInst, PhiInst> map = phiInstMap.get(block);
        for (AllocateInst alloca : map.keySet()) {
            PhiInst phiInst = map.get(alloca);
            Operand value;
            if (!renameTable.get(predecessor).containsKey(alloca)
                    || renameTable.get(predecessor).get(alloca) == null) {
                value = alloca.getType().getDefaultValue();
            } else
                value = renameTable.get(predecessor).get(alloca);
            phiInst.addBranch(value, predecessor);
        }
        if (predecessor != null) {
            for (AllocateInst alloca : allocaInst) {
                if (!map.containsKey(alloca))
                    renameTable.get(block).put(alloca, renameTable.get(predecessor).get(alloca));
            }
        }

        if (visit.contains(block))
            return;
        visit.add(block);

        for (AllocateInst alloca : map.keySet())
            renameTable.get(block).put(alloca, map.get(alloca).getResult());

        ArrayList<IRInstruction> instructions = block.getInstructions();
        for (IRInstruction instruction : instructions) {
            if (instruction instanceof LoadInst && useAlloca.containsKey(instruction)) {
                AllocateInst alloca = useAlloca.get(instruction);
                assert renameTable.containsKey(block);
                assert renameTable.get(block).containsKey(alloca);
                Operand value = renameTable.get(block).get(alloca);
                instruction.getResult().replaceUse(value);
                instruction.removeFromBlock();
            } else if (instruction instanceof StoreInst && defAlloca.containsKey(instruction)) {
                AllocateInst alloca = defAlloca.get(instruction);
                if (!renameTable.get(block).containsKey(alloca))
                    renameTable.get(block).put(alloca, ((StoreInst) instruction).getValue());
                else
                    renameTable.get(block).replace(alloca, ((StoreInst) instruction).getValue());
                instruction.removeFromBlock();
            }
        }

        for (BasicBlock successor : block.getSuccessors())
            rename(successor, block);

        for (PhiInst phiInst : map.values())
            block.addInstructionAtFront(phiInst);
    }
}
