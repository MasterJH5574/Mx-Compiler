package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.PointerType;

import java.util.*;

public class Andersen extends Pass {
    // TOT and CNT are used for calculating may-alias rate.
    static public int TOT = 0;
    static public int CNT = 0;

    public static class Node {
        private String name;
        private Set<Node> pointsTo;         // a -> b if loc(b) is in pts(a)
        private Set<Node> inclusiveEdge;    // a -> b if a <= b
        private Set<Node> dereferenceLhs;   // a -> b if *a <= b
        private Set<Node> dereferenceRhs;   // a -> b if b <= *a

        public Node(String name) {
            this.name = name;
            pointsTo = new HashSet<>();
            inclusiveEdge = new HashSet<>();
            dereferenceLhs = new HashSet<>();
            dereferenceRhs = new HashSet<>();
        }

        public String getName() {
            return name;
        }

        public Set<Node> getPointsTo() {
            return pointsTo;
        }

        public Set<Node> getInclusiveEdge() {
            return inclusiveEdge;
        }

        public Set<Node> getDereferenceLhs() {
            return dereferenceLhs;
        }

        public Set<Node> getDereferenceRhs() {
            return dereferenceRhs;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private Set<Node> nodes;
    private Map<Operand, Node> nodeMap;

    public Andersen(Module module) {
        super(module);
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional())
                return false;
        }

        nodes = new HashSet<>();
        nodeMap = new HashMap<>();
        constructNode();
        addConstraints();
        runAndersen();
        return false;
    }

    private void constructNode() {
        for (GlobalVariable globalVariable : module.getGlobalVariableMap().values()) {
            Node node = new Node(globalVariable.getFullName());
            nodeMap.put(globalVariable, node);
            nodes.add(node);
        }

        for (Function function : module.getFunctionMap().values()) {
            for (Parameter parameter : function.getParameters()) {
                if (parameter.getType() instanceof PointerType) {
                    Node node = new Node(parameter.getFullName());
                    nodeMap.put(parameter, node);
                    nodes.add(node);
                }
            }

            for (BasicBlock block : function.getBlocks()) {
                IRInstruction ptr = block.getInstHead();
                while (ptr != null) {
                    if (ptr.hasResult()) {
                        Register result = ptr.getResult();
                        if (result.getType() instanceof PointerType) {
                            Node node = new Node(result.getFullName());
                            nodeMap.put(result, node);
                            nodes.add(node);
                        }
                    }
                    ptr = ptr.getInstNext();
                }
            }
        }
    }

    private void addConstraints() {
        for (GlobalVariable globalVariable : module.getGlobalVariableMap().values()) {
            Node pointer = nodeMap.get(globalVariable);
            Node pointTo = new Node(pointer.getName() + ".globalValue");
            pointer.getPointsTo().add(pointTo);
            nodes.add(pointTo);
        }

        for (Function function : module.getFunctionMap().values()) {
            for (BasicBlock block : function.getBlocks()) {
                IRInstruction ptr = block.getInstHead();
                while (ptr != null) {
                    ptr.addConstraintsForAndersen(nodeMap, nodes);
                    ptr = ptr.getInstNext();
                }
            }
        }
    }

    private void runAndersen() {
        Queue<Node> queue = new LinkedList<>();
        Set<Node> inQueue = new HashSet<>();
        for (Node node : nodes) {
            if (!node.getPointsTo().isEmpty()) {
                queue.offer(node);
                inQueue.add(node);
            }
        }
        while (!queue.isEmpty()) {
            Node node = queue.poll();
            inQueue.remove(node);
            for (Node pointTo : node.getPointsTo()) {
                for (Node lhs : node.getDereferenceLhs()) {
                    if (!pointTo.getInclusiveEdge().contains(lhs)) {
                        pointTo.getInclusiveEdge().add(lhs);
                        if (!inQueue.contains(pointTo)) {
                            queue.offer(pointTo);
                            inQueue.add(pointTo);
                        }
                    }
                }
                for (Node rhs : node.getDereferenceRhs()) {
                    if (!rhs.getInclusiveEdge().contains(pointTo)) {
                        rhs.getInclusiveEdge().add(pointTo);
                        if (!inQueue.contains(rhs)) {
                            queue.offer(rhs);
                            inQueue.add(rhs);
                        }
                    }
                }
            }
            for (Node inclusive : node.getInclusiveEdge()) {
                if (inclusive.pointsTo.addAll(node.pointsTo)) {
                    if (!inQueue.contains(inclusive)) {
                        queue.offer(inclusive);
                        inQueue.add(inclusive);
                    }
                }
            }
        }
    }

    public boolean mayAlias(Operand op1, Operand op2) {
        if (op1 instanceof ConstNull || op2 instanceof ConstNull)
            return false;
//        TOT++;
        if (!op1.getType().equals(op2.getType()))
            return false;
        assert nodeMap.containsKey(op1);
        assert nodeMap.containsKey(op2);
        Set<Node> pointsTo1 = nodeMap.get(op1).getPointsTo();
        Set<Node> pointsTo2 = nodeMap.get(op2).getPointsTo();
//        if (!Collections.disjoint(pointsTo1, pointsTo2))
//            CNT++;
        return !Collections.disjoint(pointsTo1, pointsTo2);
    }
}
