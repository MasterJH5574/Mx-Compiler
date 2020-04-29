// Implement the pseudocode in "Modern Compiler Implementation in Java", Chapter 11.

package MxCompiler.Backend;

import MxCompiler.Optim.LoopOptim.LoopAnalysis;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Function;
import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Instruction.LoadInst;
import MxCompiler.RISCV.Instruction.MoveInst;
import MxCompiler.RISCV.Instruction.StoreInst;
import MxCompiler.RISCV.Module;
import MxCompiler.RISCV.Operand.Address.StackLocation;
import MxCompiler.RISCV.Operand.Register.PhysicalRegister;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;
import MxCompiler.Utilities.Pair;

import java.util.*;

public class RegisterAllocator extends ASMPass {
    private static class Edge extends Pair<VirtualRegister, VirtualRegister> {
        public Edge(VirtualRegister first, VirtualRegister second) {
            super(first, second);
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Edge))
                return false;
            return toString().equals(obj.toString());
        }

        @Override
        public String toString() {
            return "(" + getFirst().getName() + ", " + getSecond().getName() + ")";
        }
    }

    final private int K = 28; // K represents the number of allocatable physical registers.

    private Function function;
    private final LoopAnalysis loopAnalysis;


    public RegisterAllocator(Module module, LoopAnalysis loopAnalysis) {
        super(module);
        this.loopAnalysis = loopAnalysis;
    }

    // ------ Data Structures ------
    private Set<VirtualRegister> preColored;
    private Set<VirtualRegister> initial;
    private Queue<VirtualRegister> simplifyWorkList;
    private Queue<VirtualRegister> freezeWorkList;
    private Queue<VirtualRegister> spillWorkList;
    private Set<VirtualRegister> spilledNodes;
    private Set<VirtualRegister> coalescedNodes;
    private Set<VirtualRegister> coloredNodes;
    private Stack<VirtualRegister> selectStack;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Set<MoveInst> coalescedMoves;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Set<MoveInst> constrainedMoves;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Set<MoveInst> frozenMoves;
    private Queue<MoveInst> workListMoves;
    private Set<MoveInst> activeMoves;

    private Set<Edge> adjSet;
    // adjList is contained in every VirtualRegister.
    // degree is contained in every VirtualRegister.
    // moveList is contained in every VirtualRegister.
    // alias is contained in every VirtualRegister.
    // color is contained in every VirtualRegister.

    // ------ Data Structure End ------

    @Override
    public void run() {
        for (Function function : module.getFunctionMap().values())
            runGraphColoring(function);
    }

    private void runGraphColoring(Function function) {
        this.function = function;
        while (true) {
            initializeDataStructures();
            computeSpillCost();
            new LivenessAnalysis(module).run();
            build();
            makeWorkList();

            while (!simplifyWorkList.isEmpty()
                    || !workListMoves.isEmpty()
                    || !freezeWorkList.isEmpty()
                    || !spillWorkList.isEmpty()) {
                if (!simplifyWorkList.isEmpty())
                    simplify();
                else if (!workListMoves.isEmpty())
                    coalesce();
                else if (!freezeWorkList.isEmpty())
                    freeze();
                else
                    selectSpill();
            }
            assignColors();

            if (!spilledNodes.isEmpty())
                rewriteProgram();
            else
                break;
        }

        checkEveryVRHasAColor();
        removeRedundantMoveInst();
    }

    private void initializeDataStructures() {
        preColored = new HashSet<>();
        initial = new HashSet<>();
        simplifyWorkList = new LinkedList<>();
        freezeWorkList = new LinkedList<>();
        spillWorkList = new LinkedList<>();
        spilledNodes = new HashSet<>();
        coalescedNodes = new HashSet<>();
        coloredNodes = new HashSet<>();
        selectStack = new Stack<>();

        coalescedMoves = new HashSet<>();
        constrainedMoves = new HashSet<>();
        frozenMoves = new HashSet<>();
        workListMoves = new LinkedList<>();
        activeMoves = new HashSet<>();

        adjSet = new HashSet<>();


        initial.addAll(function.getSymbolTable().getAllVRs());
        preColored.addAll(PhysicalRegister.vrs.values());
        initial.removeAll(preColored);


        for (VirtualRegister vr : initial)
            vr.clearColoringData();
        int inf = 1000000000;
        for (VirtualRegister vr : preColored)
            vr.setDegree(inf);
    }

    // Compute the spill cost of every virtual register(\sum (10^depth * number of defs/uses)).
    private void computeSpillCost() {
        ArrayList<BasicBlock> dfsOrder = function.getDFSOrder();
        for (BasicBlock block : dfsOrder) {
            int depth = loopAnalysis.getBlockDepth(block);
            ASMInstruction ptr = block.getInstHead();
            while (ptr != null) {
                for (VirtualRegister def : ptr.getDef())
                    def.increaseSpillCost(Math.pow(10, depth));
                for (VirtualRegister use : ptr.getUse())
                    use.increaseSpillCost(Math.pow(10, depth));
                ptr = ptr.getNextInst();
            }
        }
    }

    // Build interference graph.
    private void build() {
        ArrayList<BasicBlock> dfsOrder = function.getDFSOrder();
        for (BasicBlock block : dfsOrder) {
            Set<VirtualRegister> live = block.getVRLiveOut();
            ASMInstruction ptr = block.getInstTail();
            while (ptr != null) {
                if (ptr instanceof MoveInst) {
                    live.removeAll(ptr.getUse());
                    for (VirtualRegister n : ptr.getDefUseUnion())
                        n.getMoveList().add(((MoveInst) ptr));
                    workListMoves.offer(((MoveInst) ptr));
                }

                live.addAll(ptr.getDef());
                for (VirtualRegister d : ptr.getDef()) {
                    for (VirtualRegister l : live)
                        addEdge(l, d);
                }
                live.removeAll(ptr.getDef());
                live.addAll(ptr.getUse());

                ptr = ptr.getPrevInst();
            }
        }
    }

    // Add edges (u, v) & (v, u) to interference graph.
    private void addEdge(VirtualRegister u, VirtualRegister v) {
        if (!adjSet.contains(new Edge(u, v))
                && u != v
                && u != PhysicalRegister.zeroVR
                && v != PhysicalRegister.zeroVR) {
            adjSet.add(new Edge(u, v));
            adjSet.add(new Edge(v, u));
            if (!preColored.contains(u)) {
                u.getAdjList().add(v);
                u.increaseDegree();
            }
            if (!preColored.contains(v)) {
                v.getAdjList().add(u);
                v.increaseDegree();
            }
        }
    }

    // For each virtual register which is not pre-colored, add it to one of the work lists.
    private void makeWorkList() {
        for (VirtualRegister n : initial) {
            initial.remove(n);
            if (n.getDegree() >= K)
                spillWorkList.offer(n);
            else if (moveRelated(n))
                freezeWorkList.offer(n);
            else
                simplifyWorkList.offer(n);
        }
    }

    // Get the current neighbors of a virtual register n.
    private Set<VirtualRegister> adjacent(VirtualRegister n) {
        Set<VirtualRegister> res = new HashSet<>(n.getAdjList());
        res.removeAll(selectStack);
        res.removeAll(coalescedNodes);
        return res;
    }

    // Get the current move instructions related to a virtual register n.
    private Set<MoveInst> nodeMoves(VirtualRegister n) {
        Set<MoveInst> res = new HashSet<>(activeMoves);
        res.addAll(workListMoves);
        res.retainAll(n.getMoveList());
        return res;
    }

    // Check whether a virtual register n has related move instructions.
    private boolean moveRelated(VirtualRegister n) {
        return !nodeMoves(n).isEmpty();
    }

    // Remove a node whose current degree is no more than K from the interference graph.
    private void simplify() {
        assert !simplifyWorkList.isEmpty();
        VirtualRegister n = simplifyWorkList.poll();
        selectStack.push(n);
        for (VirtualRegister m : adjacent(n))
            decrementDegree(m);
    }

    // Decrease the degree of m by 1.
    private void decrementDegree(VirtualRegister m) {
        int d = m.getDegree();
        m.setDegree(d - 1);
        if (d == K) {
            Set<VirtualRegister> union = new HashSet<>(adjacent(m));
            union.add(m);
            enableMoves(union);
            spillWorkList.remove(m);
            if (moveRelated(m))
                freezeWorkList.offer(m);
            else
                simplifyWorkList.offer(m);
        }
    }

    // Move some move instructions related to virtual register in nodes to workListMoves.
    private void enableMoves(Set<VirtualRegister> nodes) {
        for (VirtualRegister n : nodes) {
            for (MoveInst m : nodeMoves(n)) {
                if (activeMoves.contains(m)) {
                    activeMoves.remove(m);
                    workListMoves.offer(m);
                }
            }
        }
    }

    // Move a virtual register u from freezeWorkList to simplifyWorkList.
    private void addWorkList(VirtualRegister u) {
        if (!preColored.contains(u) && !moveRelated(u) && u.getDegree() < K) {
            freezeWorkList.remove(u);
            simplifyWorkList.add(u);
        }
    }

    // George's condition for conservative coalescing.
    private boolean OK(VirtualRegister t, VirtualRegister r) {
        return t.getDegree() < K || preColored.contains(t) || adjSet.contains(new Edge(t, r));
    }

    // Briggs's condition for conservative coalescing.
    private boolean conservative(Set<VirtualRegister> nodes) {
        int k = 0;
        for (VirtualRegister n : nodes) {
            if (n.getDegree() >= K)
                k++;
        }
        return k < K;
    }

    // Try to coalesce rd and rs of a move instruction in workListMoves.
    private void coalesce() {
        assert !workListMoves.isEmpty();
        MoveInst m = workListMoves.poll();
        VirtualRegister x = getAlias(((VirtualRegister) m.getRd()));
        VirtualRegister y = getAlias(((VirtualRegister) m.getRs()));

        VirtualRegister u;
        VirtualRegister v;
        if (preColored.contains(y)) {
            u = y;
            v = x;
        } else {
            u = x;
            v = y;
        }

        Set<VirtualRegister> unionAdjacentNode = new HashSet<>(adjacent(u));
        unionAdjacentNode.addAll(adjacent(v));
        if (u == v) {
            coalescedMoves.add(m);
            addWorkList(u);
        } else if (preColored.contains(v) || adjSet.contains(new Edge(u, v))) {
            constrainedMoves.add(m);
            addWorkList(u);
            addWorkList(v);
        } else if ((preColored.contains(u) && anyAdjacentNodeIsOK(v, u))
                && (!preColored.contains(u) && conservative(unionAdjacentNode))) {
            coalescedMoves.add(m);
            combine(u, v);
            addWorkList(u);
        } else
            activeMoves.add(m);
    }

    // Check whether any adjacent node of v "t" satisfies that OK(t, u) is true.
    private boolean anyAdjacentNodeIsOK(VirtualRegister v, VirtualRegister u) {
        for (VirtualRegister t : adjacent(v)) {
            if (!OK(t, u))
                return false;
        }
        return true;
    }

    // Coalesce virtual registers u and v, where u may be pre-colored.
    private void combine(VirtualRegister u, VirtualRegister v) {
        if (freezeWorkList.contains(v))
            freezeWorkList.remove(v);
        else
            spillWorkList.remove(v);
        coalescedNodes.add(v);
        v.setAlias(u);
        u.getMoveList().addAll(v.getMoveList());

        Set<VirtualRegister> nodes = new HashSet<>();
        nodes.add(v);
        enableMoves(nodes);

        for (VirtualRegister t : adjacent(v)) {
            addEdge(t, u);
            decrementDegree(t);
        }
        if (u.getDegree() >= K && freezeWorkList.contains(u)) {
            freezeWorkList.remove(u);
            spillWorkList.add(u);
        }
    }

    // Get the alias of n. It is just a union-find set, so path contraction can be applied.
    private VirtualRegister getAlias(VirtualRegister n) {
        if (coalescedNodes.contains(n)) {
            VirtualRegister alias = getAlias(n.getAlias());
            n.setAlias(alias);
            return alias;
        } else
            return n;
    }

    // Try to freeze a virtual register so that coalescing is given up.
    private void freeze() {
        VirtualRegister u = freezeWorkList.poll();
        simplifyWorkList.add(u);
        freezeMoves(u);
    }

    // Freeze a virtual register u.
    private void freezeMoves(VirtualRegister u) {
        for (MoveInst m : nodeMoves(u)) {
            VirtualRegister x = ((VirtualRegister) m.getRd());
            VirtualRegister y = ((VirtualRegister) m.getRs());

            VirtualRegister v;
            if (getAlias(y) == getAlias(u))
                v = getAlias(x);
            else
                v = getAlias(y);
            activeMoves.remove(m);
            frozenMoves.add(m);

            if (freezeWorkList.contains(v) && nodeMoves(v).isEmpty()) { // In "Implementation in C",
                                                                        // v.getDegree() < K ?
                freezeWorkList.remove(v);
                simplifyWorkList.add(v);
            }
        }
    }

    // Select a virtual register from spillWorkList and then spill it.
    private void selectSpill() {
        VirtualRegister m = selectVRToBeSpilled();
        spillWorkList.remove(m);
        simplifyWorkList.add(m);
        freezeMoves(m);
    }

    // Select a optimal virtual register to spill using spill metric.
    private VirtualRegister selectVRToBeSpilled() {
        double minRatio = Double.POSITIVE_INFINITY;
        VirtualRegister spilledVR = null;
        for (VirtualRegister vr : spillWorkList) {
            double spillRatio = vr.computeSpillRatio();
            if (spillRatio <= minRatio) {
                minRatio = spillRatio;
                spilledVR = vr;
            }
        }
        assert spilledVR != null;
        return spilledVR;
    }

    private void assignColors() {
        while (!selectStack.isEmpty()) {
            VirtualRegister n = selectStack.pop();
            Set<PhysicalRegister> okColors = new LinkedHashSet<>(PhysicalRegister.allocatablePRs.values());
            for (VirtualRegister w : n.getAdjList()) {
                Set<VirtualRegister> union = new HashSet<>(coloredNodes);
                union.addAll(preColored);
                if (union.contains(w))
                    okColors.remove(getAlias(w).getColorPR());
            }

            if (okColors.isEmpty())
                spilledNodes.add(n);
            else {
                coloredNodes.add(n);
                PhysicalRegister c = selectColor(okColors);
                n.setColorPR(c);
            }
        }
        for (VirtualRegister n : coalescedNodes)
            n.setColorPR(n.getAlias().getColorPR());
    }

    // Select an unused physical register, with caller-save registers always being selected first.
    private PhysicalRegister selectColor(Set<PhysicalRegister> okColors) {
        assert !okColors.isEmpty();
        for (PhysicalRegister pr : okColors) {
            if (PhysicalRegister.callerSavePRs.containsKey(pr.getName()))
                return pr;
        }
        return okColors.iterator().next();
    }

    private void rewriteProgram() {
        for (VirtualRegister vr : spilledNodes) {
            StackLocation stackLocation = new StackLocation(vr.getName());
            function.getStackFrame().getSpillLocations().put(vr, stackLocation);
            Set<ASMInstruction> defs = new HashSet<>(vr.getDef().keySet());
            Set<ASMInstruction> uses = new HashSet<>(vr.getUse().keySet());

            int cnt = 0;
            for (ASMInstruction inst : defs) {
                VirtualRegister spilledVR = new VirtualRegister(vr.getName() + ".spill" + cnt);
                function.getSymbolTable().putASMRename(spilledVR.getName(), spilledVR);
                cnt++;

                BasicBlock block = inst.getBasicBlock();
                inst.replaceDef(vr, spilledVR);
                block.addInstructionNext(inst, new StoreInst(block, spilledVR, StoreInst.ByteSize.sw, stackLocation));
            }
            for (ASMInstruction inst : uses) {
                VirtualRegister spilledVR = new VirtualRegister(vr.getName() + ".spill" + cnt);
                function.getSymbolTable().putASMRename(spilledVR.getName(), spilledVR);
                cnt++;

                BasicBlock block = inst.getBasicBlock();
                inst.replaceUse(vr, spilledVR);
                block.addInstructionPrev(inst, new LoadInst(block, spilledVR, LoadInst.ByteSize.lw, stackLocation));
            }
            assert vr.getDef().isEmpty() && vr.getUse().isEmpty();
            function.getSymbolTable().removeVR(vr);
        }
    }

    private void checkEveryVRHasAColor() {
        ArrayList<BasicBlock> dfsOrder = function.getDFSOrder();
        for (BasicBlock block : dfsOrder) {
            ASMInstruction ptr = block.getInstHead();
            while (ptr != null) {
                for (VirtualRegister vr : ptr.getDef())
                    assert vr.hasAColor();
                for (VirtualRegister vr : ptr.getUse())
                    assert vr.hasAColor();
                ptr = ptr.getNextInst();
            }
        }
    }

    private void removeRedundantMoveInst() {
        /*
        ArrayList<BasicBlock> dfsOrder = function.getDFSOrder();
        for (BasicBlock block : dfsOrder) {
            ASMInstruction ptr = block.getInstHead();
            while (ptr != null) {
                ASMInstruction next = ptr.getNextInst();
//                if (ptr instanceof MoveInst && ((MoveInst) ptr).getRd().getco)
            }
        }
        */
    }
}
