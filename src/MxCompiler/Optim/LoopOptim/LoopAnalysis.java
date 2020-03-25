package MxCompiler.Optim.LoopOptim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.BranchInst;
import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Instruction.PhiInst;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.*;
import MxCompiler.Optim.Pass;
import MxCompiler.Utilities.Pair;

import java.util.*;

public class LoopAnalysis extends Pass {
    static public class LoopNode {
        static LoopAnalysis loopAnalysis;

        private BasicBlock header;
        private Set<BasicBlock> loopBlocks;
        private Set<BasicBlock> uniqueLoopBlocks;
        private Set<BasicBlock> exitBlocks;

        private LoopNode father;
        private ArrayList<LoopNode> children;

        private BasicBlock preHeader;

        public LoopNode(BasicBlock header) {
            this.header = header;
            this.loopBlocks = new HashSet<>();
            this.uniqueLoopBlocks = null;
            this.exitBlocks = null;
            this.father = null;
            this.children = new ArrayList<>();
            this.preHeader = null;
        }

        public void addLoopBlock(BasicBlock block) {
            this.loopBlocks.add(block);
        }

        public Set<BasicBlock> getLoopBlocks() {
            return loopBlocks;
        }

        public Set<BasicBlock> getUniqueLoopBlocks() {
            return uniqueLoopBlocks;
        }

        public Set<BasicBlock> getExitBlocks() {
            return exitBlocks;
        }

        public void setExitBlocks(Set<BasicBlock> exitBlocks) {
            this.exitBlocks = exitBlocks;
        }

        public void setFather(LoopNode father) {
            this.father = father;
        }

        public boolean hasFather() {
            return father != null;
        }

        public void addChild(LoopNode child) {
            children.add(child);
        }

        public ArrayList<LoopNode> getChildren() {
            return children;
        }

        public boolean hasPreHeader(Map<BasicBlock, LoopNode> blockNodeMap) {
            if (preHeader != null)
                return true;

            /*
             * If header has only one predecessor out of block,
             * and the only predecessor has only one successor(header),
             * then we can mark the only predecessor as preHeader.
             */
            int predecessorCnt = 0;
            int successorCnt = 0;
            BasicBlock mayPreHeader = null;
            for (BasicBlock predecessor : header.getPredecessors()) {
                if (loopBlocks.contains(predecessor))
                    continue;
                predecessorCnt++;
                successorCnt = predecessor.getSuccessors().size();
                mayPreHeader = predecessor;
            }

            if (predecessorCnt == 1 && successorCnt == 1) {
                preHeader = mayPreHeader;
                loopAnalysis.preHeaders.add(preHeader);
                assert blockNodeMap.containsKey(preHeader) && blockNodeMap.get(preHeader) == this.father;
                return true;
            } else
                return false;
        }

        public void addPreHeader(Map<BasicBlock, LoopNode> blockNodeMap) {
            Function function = header.getFunction();
            preHeader = new BasicBlock(function, "preHeaderOf" + header.getNameWithoutDot());
            loopAnalysis.preHeaders.add(preHeader);
            function.getSymbolTable().put(preHeader.getName(), preHeader);

            // Deal with PhiInst.
            IRInstruction ptr = header.getInstHead();
            while (ptr instanceof PhiInst) {
                assert ptr.hasResult();
                Register result = ptr.getResult();
                Register newResult = new Register(result.getType(), result.getNameWithoutDot());
                PhiInst phiInst = new PhiInst(preHeader, new LinkedHashSet<>(), newResult);
                function.getSymbolTable().put(newResult.getName(), newResult);

                ArrayList<Pair<Operand, BasicBlock>> removeList = new ArrayList<>();
                for (Pair<Operand, BasicBlock> branch : ((PhiInst) ptr).getBranch()) {
                    if (loopBlocks.contains(branch.getSecond()))
                        continue;
                    phiInst.addBranch(branch.getFirst(), branch.getSecond());
                    removeList.add(branch);
                }
                preHeader.addInstruction(phiInst);

                for (Pair<Operand, BasicBlock> pair : removeList)
                    ((PhiInst) ptr).removeIncomingBranch(pair);
                ((PhiInst) ptr).addBranch(newResult, preHeader);

                ptr = ptr.getInstNext();
            }

            // Deal with predecessor and successor.
            ArrayList<BasicBlock> removeList = new ArrayList<>();
            for (BasicBlock predecessor : header.getPredecessors()) {
                if (loopBlocks.contains(predecessor))
                    continue;
                IRInstruction branchInst = predecessor.getInstTail();
                assert branchInst instanceof BranchInst;
                branchInst.replaceUse(header, preHeader);

                predecessor.getSuccessors().remove(header);
                predecessor.getSuccessors().add(preHeader);
                preHeader.getPredecessors().add(predecessor);
                removeList.add(predecessor);
            }
            for (BasicBlock block : removeList)
                header.getPredecessors().remove(block);

            preHeader.addInstruction(new BranchInst(preHeader, null, header, null));
            assert header.getPrev() != null;
            header.getPrev().setNext(preHeader);
            preHeader.setPrev(header.getPrev());
            header.setPrev(preHeader);
            preHeader.setNext(header);

            blockNodeMap.put(preHeader, this.father);
        }

        public BasicBlock getPreHeader() {
            return preHeader;
        }

        public void mergeLoopNode(LoopNode loop) {
            assert this.header == loop.header;
            this.loopBlocks.addAll(loop.loopBlocks);
        }

        public void removeUniqueLoopBlocks(LoopNode child) {
            assert uniqueLoopBlocks.containsAll(child.loopBlocks);
            uniqueLoopBlocks.removeAll(child.loopBlocks);
        }

        public boolean defOutOfLoop(Operand operand) {
            if (operand instanceof Parameter || operand instanceof Constant || operand instanceof GlobalVariable)
                return true;
            assert operand instanceof Register;
            return !this.loopBlocks.contains(((Register) operand).getDef().getBasicBlock());
        }

        @Override
        public String toString() {
            return header.getName();
        }
    }

    private Map<Function, LoopNode> loopRoot;
    private Map<BasicBlock, LoopNode> blockNodeMap;
    private Map<BasicBlock, LoopNode> headerNodeMap;
    private Set<BasicBlock> preHeaders;

    public LoopAnalysis(Module module) {
        super(module);
        LoopNode.loopAnalysis = this;
    }

    public Map<Function, LoopNode> getLoopRoot() {
        return loopRoot;
    }

    public Map<BasicBlock, LoopNode> getBlockNodeMap() {
        return blockNodeMap;
    }

    public boolean isPreHeader(BasicBlock block) {
        return preHeaders != null && preHeaders.contains(block);
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional())
                return false;
        }

        loopRoot = new HashMap<>();
        blockNodeMap = new HashMap<>();
        headerNodeMap = new HashMap<>();
        preHeaders = new HashSet<>();
        for (Function function : module.getFunctionMap().values())
            loopRoot.put(function, constructLoopTree(function));


        return false;
    }

    private LoopNode constructLoopTree(Function function) {
        LoopNode root = new LoopNode(function.getEntranceBlock());
        loopRoot.put(function, root);

        dfsDetectNaturalLoop(function.getEntranceBlock(), new HashSet<>(), root);
        dfsConstructLoopTree(function.getEntranceBlock(), new HashSet<>(), root);
        dfsLoopTree(root);

        return root;
    }

    private void dfsDetectNaturalLoop(BasicBlock block, Set<BasicBlock> visit, LoopNode root) {
        visit.add(block);
        root.addLoopBlock(block);
        for (BasicBlock successor : block.getSuccessors()) {
            if (successor.dominate(block)) {
                // Means that a back edge is found.
                extractNaturalLoop(successor, block);
            } else if (!visit.contains(successor))
                dfsDetectNaturalLoop(successor, visit, root);
        }
    }

    private void extractNaturalLoop(BasicBlock header, BasicBlock end) {
        LoopNode loop = new LoopNode(header);

        HashSet<BasicBlock> visit = new HashSet<>();
        Queue<BasicBlock> queue = new LinkedList<>();
        queue.offer(end);
        visit.add(end);
        while (!queue.isEmpty()) {
            BasicBlock block = queue.poll();
            if (header.dominate(block))
                loop.addLoopBlock(block);

            for (BasicBlock predecessor : block.getPredecessors()) {
                if (predecessor != header && !visit.contains(predecessor)) {
                    queue.offer(predecessor);
                    visit.add(predecessor);
                }
            }
        }
        loop.addLoopBlock(header);

        if (!headerNodeMap.containsKey(header))
            headerNodeMap.put(header, loop);
        else
            headerNodeMap.get(header).mergeLoopNode(loop);
    }

    private void dfsConstructLoopTree(BasicBlock block, Set<BasicBlock> visit, LoopNode currentLoop) {
        visit.add(block);

        LoopNode child = null;
        if (block == currentLoop.header) {
            // block == entrance block
            currentLoop.uniqueLoopBlocks = new HashSet<>(currentLoop.loopBlocks);
        } else if (headerNodeMap.containsKey(block)) {
            child = headerNodeMap.get(block);
            child.setFather(currentLoop);
            currentLoop.addChild(child);

            currentLoop.removeUniqueLoopBlocks(child);
            child.uniqueLoopBlocks = new HashSet<>(child.loopBlocks);
        }

        for (BasicBlock successor : block.getSuccessors()) {
            if (!visit.contains(successor)) {
                LoopNode nextLoop = child != null ? child : currentLoop;
                while (nextLoop != null && !nextLoop.loopBlocks.contains(successor))
                    nextLoop = nextLoop.father;
                assert nextLoop != null;

                dfsConstructLoopTree(successor, visit, nextLoop);
            }
        }
    }

    private void dfsLoopTree(LoopNode loop) {
        for (BasicBlock block : loop.uniqueLoopBlocks)
            blockNodeMap.put(block, loop);

        for (LoopNode child : loop.children)
            dfsLoopTree(child);
        if (loop.hasFather() && !loop.hasPreHeader(blockNodeMap))
            loop.addPreHeader(blockNodeMap);

        Set<BasicBlock> exitBlocks = new HashSet<>();
        if (loop.hasFather()) {
            for (LoopNode child : loop.children) {
                for (BasicBlock exit : child.exitBlocks) {
                    assert exit.getInstTail() instanceof BranchInst;
                    BranchInst exitInst = ((BranchInst) exit.getInstTail());
                    if (!loop.getLoopBlocks().contains(exitInst.getThenBlock())) {
                        exitBlocks.add(exit);
                        break;
                    }
                    if (exitInst.isConditional() && !loop.getLoopBlocks().contains(exitInst.getElseBlock())) {
                        exitBlocks.add(exit);
                        break;
                    }
                }
            }
            for (BasicBlock exit : loop.getUniqueLoopBlocks()) {
                assert exit.getInstTail() instanceof BranchInst;
                BranchInst exitInst = ((BranchInst) exit.getInstTail());
                if (!loop.getLoopBlocks().contains(exitInst.getThenBlock())) {
                    exitBlocks.add(exit);
                    break;
                }
                if (exitInst.isConditional() && !loop.getLoopBlocks().contains(exitInst.getElseBlock())) {
                    exitBlocks.add(exit);
                    break;
                }
            }
        }
        loop.setExitBlocks(exitBlocks);
    }
}
