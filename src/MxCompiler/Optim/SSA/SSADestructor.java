package MxCompiler.Optim.SSA;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.Optim.Pass;
import MxCompiler.Utilities.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class SSADestructor extends Pass {
    public SSADestructor(Module module) {
        super(module);
    }

    @Override
    public boolean run() {
        for (Function function : module.getFunctionMap().values()) {
            splitCriticalEdges(function);
            sequentializePC(function);
        }

        return false;
    }

    private void splitCriticalEdges(Function function) {
        for (BasicBlock block : function.getDFSOrder()) {
            Set<BasicBlock> predecessors = new HashSet<>(block.getPredecessors());
            if (predecessors.size() == 0)
                continue;

            ArrayList<PhiInst> phiNodes = new ArrayList<>();
            IRInstruction phiPtr = block.getInstHead();
            while (phiPtr instanceof PhiInst) {
                phiNodes.add(((PhiInst) phiPtr));
                phiPtr = phiPtr.getInstNext();
            }
            if (phiNodes.size() == 0)
                continue;

            if (predecessors.size() == 1) {
                for (PhiInst phi : phiNodes) {
                    assert phi.getBranch().size() == 1;
                    phi.getResult().replaceUse(phi.getBranch().iterator().next().getFirst());
                    phi.removeFromBlock();
                }
                continue;
            }

            for (BasicBlock predecessor : predecessors) {
                ParallelCopyInst pc = new ParallelCopyInst(predecessor);
                if (predecessor.getSuccessors().size() > 1) {
                    // critical edge
                    BasicBlock criticalBlock = new BasicBlock(block.getFunction(), "criticalBlock");
                    block.getFunction().getSymbolTable().put(criticalBlock.getName(), criticalBlock);
                    BranchInst branch = new BranchInst(criticalBlock, null, block, null);

                    criticalBlock.addInstruction(pc);
                    criticalBlock.addInstruction(branch);

                    if (predecessor.getInstTail() instanceof BranchInst)
                        predecessor.getInstTail().replaceUse(block, criticalBlock);

                    block.getPredecessors().remove(predecessor);
                    block.getPredecessors().add(criticalBlock);
                    predecessor.getSuccessors().remove(block);
                    predecessor.getSuccessors().add(criticalBlock);
                    for (PhiInst phi : phiNodes)
                        phi.replaceUse(predecessor, criticalBlock);

                    block.getFunction().addBasicBlockPrev(block, criticalBlock);
                } else {
                    if (predecessor.notEndWithTerminalInst())
                        predecessor.addInstruction(pc);
                    else
                        predecessor.addInstructionPrev(predecessor.getInstTail(), pc);
                }
            }

            for (PhiInst phi : phiNodes) {
                for (Pair<Operand, BasicBlock> branch : phi.getBranch()) {
                    BasicBlock predecessor = branch.getSecond();
                    Operand source = branch.getFirst();
                    predecessor.getParallelCopy().appendMove(new MoveInst(predecessor, source, phi.getResult()));
                }
                phi.removeFromBlock();
            }
        }
    }

    private void sequentializePC(Function function) {
        for (BasicBlock block : function.getBlocks()) {
            ParallelCopyInst pc = block.getParallelCopy();
            if (pc == null)
                continue;

            ArrayList<MoveInst> moves = new ArrayList<>();
            while (!pc.getMoves().isEmpty()) {
                MoveInst move = pc.findValidMove();
                if (move != null) {
                    moves.add(move);
                    pc.removeMove(move);
                } else {
                    move = pc.getMoves().iterator().next();
                    Operand source = move.getSource();

                    Register cycle = new Register(source.getType(), "breakCycle");
                    function.getSymbolTable().put(cycle.getName(), cycle);

                    moves.add(new MoveInst(block, source, cycle));
                    move.setSource(cycle);
                }
            }
            if (block.notEndWithTerminalInst()) {
                for (MoveInst move : moves)
                    block.addInstruction(move);
            } else {
                for (MoveInst move : moves)
                    block.addInstructionPrev(block.getInstTail(), move);
            }
            pc.removeFromBlock();
        }
    }
}
