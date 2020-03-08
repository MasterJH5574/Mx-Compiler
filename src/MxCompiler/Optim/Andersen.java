package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.GlobalVariable;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.PointerType;

import java.util.*;

public class Andersen extends Pass {
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

    public void print() {
        
    }
}
