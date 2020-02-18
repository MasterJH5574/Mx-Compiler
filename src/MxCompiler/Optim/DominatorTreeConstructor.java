package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Module;
import MxCompiler.Utilities.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DominatorTreeConstructor extends Pass {
    private Map<BasicBlock, Pair<BasicBlock, BasicBlock>> disjointSet; // first for father
                                                                       // second for the min semi dom dfn node.

    public DominatorTreeConstructor(Module module) {
        super(module);
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            constructDominatorTree(function);
            constructDominanceFrontier(function);
        }
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
}
