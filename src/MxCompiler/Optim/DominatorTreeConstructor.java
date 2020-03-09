package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.BranchInst;
import MxCompiler.IR.Module;
import MxCompiler.Utilities.Pair;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class DominatorTreeConstructor extends Pass {
    private Map<BasicBlock, Pair<BasicBlock, BasicBlock>> disjointSet; // first for father
                                                                       // second for the min semi dom dfn node.

    public DominatorTreeConstructor(Module module) {
        super(module);
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional())
                return false;
        }

        for (Function function : module.getFunctionMap().values()) {
            constructDominatorTree(function);
            constructDominanceFrontier(function);
        }
        print();
        return true;
    }

    private Pair<BasicBlock, BasicBlock> updateDisjointSet(BasicBlock block) {
        Pair<BasicBlock, BasicBlock> pair = disjointSet.get(block);
        if (pair.getFirst() == block)
            return new Pair<>(block, block);
        Pair<BasicBlock, BasicBlock> res = updateDisjointSet(pair.getFirst());
        BasicBlock father = res.getFirst();
        BasicBlock minSemiDomDfnNode = res.getSecond();

        pair.setFirst(father);
        if (minSemiDomDfnNode.getSemiDom().getDfn() < pair.getSecond().getSemiDom().getDfn())
            pair.setSecond(minSemiDomDfnNode);
        return pair;
    }

    private void constructDominatorTree(Function function) {
        ArrayList<BasicBlock> dfsOrder = function.getDFSOrder();
        disjointSet = new HashMap<>();
        for (BasicBlock block : dfsOrder) {
            disjointSet.put(block, new Pair<>(block, block));
            block.setIdom(null);
            block.setSemiDom(block);
            block.setSemiDomChildren(new ArrayList<>());
        }

        for (int i = dfsOrder.size() - 1; i > 0; i--) {
            BasicBlock block = dfsOrder.get(i);
            assert block.getDfn() == i;

            for (BasicBlock predecessor : block.getPredecessors()) {
                if (predecessor.getDfn() < block.getDfn()) {
                    if (predecessor.getDfn() < block.getSemiDom().getDfn())
                        block.setSemiDom(predecessor);
                } else {
                    Pair<BasicBlock, BasicBlock> updateResult = updateDisjointSet(predecessor);
                    if (updateResult.getSecond().getSemiDom().getDfn() < block.getSemiDom().getDfn())
                        block.setSemiDom(updateResult.getSecond().getSemiDom());
                }
            }

            BasicBlock father = block.getDfsFather();
            block.getSemiDom().getSemiDomChildren().add(block);
            disjointSet.get(block).setFirst(father);

            for (BasicBlock semiDomChild : father.getSemiDomChildren()) {
                Pair<BasicBlock, BasicBlock> updateResult = updateDisjointSet(semiDomChild);
                if (updateResult.getSecond().getSemiDom() == semiDomChild.getSemiDom())
                    semiDomChild.setIdom(semiDomChild.getSemiDom());
                else
                    semiDomChild.setIdom(updateResult.getSecond());
            }
        }

        for (int i = 1; i < dfsOrder.size(); i++) {
            BasicBlock block = dfsOrder.get(i);
            if (block.getIdom() != block.getSemiDom())
                block.setIdom(block.getIdom().getIdom());
        }
        for (BasicBlock block : dfsOrder) {
            HashSet<BasicBlock> strictDominators = new HashSet<>();
            BasicBlock ptr = block.getIdom();
            while (ptr != null) {
                strictDominators.add(ptr);
                ptr = ptr.getIdom();
            }
            block.setStrictDominators(strictDominators);
        }
    }

    private void constructDominanceFrontier(Function function) {
        ArrayList<BasicBlock> blocks = function.getBlocks();
        for (BasicBlock block : blocks)
            block.setDF(new HashSet<>());

        for (BasicBlock block : blocks)
            for (BasicBlock predecessor : block.getPredecessors()) {
                BasicBlock ptr = predecessor;
                while (!block.getStrictDominators().contains(ptr)) {
                    ptr.getDF().add(block);
                    ptr = ptr.getIdom();
                }
            }
    }

    private void print() {
        OutputStream os;
        PrintWriter writer;
        String indent = "    ";
        try {
            os = new FileOutputStream("test/dominator.txt");
            writer = new PrintWriter(os);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        for (Function function : module.getFunctionMap().values()) {
            writer.println("Function: " + function.getName());
            // ------ Print CFG ------
            writer.println("  Print CFG:");
            for (BasicBlock block : function.getBlocks()) {
                if (block.getInstTail() instanceof BranchInst) {
                    BranchInst branch = (BranchInst) block.getInstTail();
                    writer.println(indent + block.getName() + "\t--->\t" + branch.getThenBlock().getName());
                    if (branch.isConditional())
                        writer.println(indent + block.getName() + "\t--->\t" + branch.getElseBlock().getName());
                }
            }
            writer.println("");

            // ------ Print Dominator Tree ------
            writer.println("  Print Dominator Tree:");
            for (BasicBlock block : function.getBlocks()) {
                if (block.getIdom() != null)
                    writer.println(indent + block.getName() + ":\t\t" + block.getIdom().getName());
//                    writer.println(indent + block.getIdom().getName() + "\t--->\t" + block.getName());
            }
            writer.println("");

            // ------ Print Strict Dominators ------
            writer.println("  Print Strict Dominators:");
            for (BasicBlock block : function.getBlocks()) {
                writer.println(indent + block.getName() + "'s strict dominators:");
                for (BasicBlock dominator : block.getStrictDominators())
                    writer.println(indent + indent + dominator.getName());
                writer.println("");
            }
            writer.println("");

            // ------ Print Dominance Frontier ------
            writer.println("  Print Dominance Frontier:");
            for (BasicBlock block : function.getBlocks()) {
                writer.println(indent + block.getName() + "'s dominance frontier:");
                for (BasicBlock df : block.getDF())
                    writer.println(indent + indent + df.getName());
                writer.println("");
            }
            writer.println("");
        }


        try {
            writer.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}
