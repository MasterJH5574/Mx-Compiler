package MxCompiler.RISCV;

import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Instruction.MoveInst;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.RISCV.Operand.Address.BaseOffsetAddr;
import MxCompiler.RISCV.Operand.Register.PhysicalRegister;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;
import MxCompiler.Utilities.SymbolTable;

import java.util.*;

public class Function {
    private Module module;

    private String name;
    private StackFrame stackFrame;

    private BasicBlock entranceBlock;
    private BasicBlock exitBlock;

    private Map<String, BasicBlock> blockMap;
    private SymbolTable symbolTable;
    private Set<PhysicalRegister> usedCalleeRegister;

    private Map<VirtualRegister, BaseOffsetAddr> gepAddrMap;

    public Function(Module module, String name, MxCompiler.IR.Function IRFunction) {
        this.module = module;
        this.name = name;
        this.stackFrame = null;

        if (IRFunction == null)
            return;

        usedCalleeRegister = new HashSet<>();
        gepAddrMap = new HashMap<>();


        int functionCnt = module.getFunctionMap().size();
        int blockCnt = 0;
        blockMap = new HashMap<>();
        ArrayList<MxCompiler.IR.BasicBlock> IRBlocks = IRFunction.getBlocks();
        for (MxCompiler.IR.BasicBlock IRBlock : IRBlocks) {
            BasicBlock block = new BasicBlock(this, IRBlock, IRBlock.getName(),
                    ".LBB" + functionCnt + "_" + blockCnt);
            this.addBasicBlock(block);
            blockMap.put(block.getName(), block);
            blockCnt++;
        }
        for (MxCompiler.IR.BasicBlock IRBlock : IRBlocks) {
            BasicBlock block = blockMap.get(IRBlock.getName());
            Set<BasicBlock> predecessors = block.getPredecessors();
            Set<BasicBlock> successors = block.getSuccessors();

            for (MxCompiler.IR.BasicBlock predecessor : IRBlock.getPredecessors())
                predecessors.add(blockMap.get(predecessor.getName()));
            for (MxCompiler.IR.BasicBlock successor : IRBlock.getSuccessors())
                successors.add(blockMap.get(successor.getName()));
        }
        entranceBlock = blockMap.get(IRBlocks.get(0).getName());
        exitBlock = blockMap.get(IRBlocks.get(IRBlocks.size() - 1).getName());


        symbolTable = new SymbolTable();
        for (Parameter parameter : IRFunction.getParameters()) {
            VirtualRegister vr = new VirtualRegister(parameter.getName());
            symbolTable.putASM(parameter.getName(), vr);
        }
        for (MxCompiler.IR.BasicBlock IRBlock : IRBlocks) {
            IRInstruction ptr = IRBlock.getInstHead();
            while (ptr != null) {
                if (ptr.hasResult()) {
                    String registerName = ptr.getResult().getName();
                    if (!(ptr instanceof MoveInst)) {
                        VirtualRegister vr = new VirtualRegister(registerName);
                        symbolTable.putASM(registerName, vr);
                    } else { // "Move" is special.
                        if (!symbolTable.contains(registerName)) {
                            VirtualRegister vr = new VirtualRegister(registerName);
                            symbolTable.putASM(registerName, vr);
                        }
                    }
                }
                ptr = ptr.getInstNext();
            }
        }
    }

    public String getName() {
        return name;
    }

    public StackFrame getStackFrame() {
        return stackFrame;
    }

    public void setStackFrame(StackFrame stackFrame) {
        this.stackFrame = stackFrame;
    }

    public BasicBlock getEntranceBlock() {
        return entranceBlock;
    }

    public Map<String, BasicBlock> getBlockMap() {
        return blockMap;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public Map<VirtualRegister, BaseOffsetAddr> getGepAddrMap() {
        return gepAddrMap;
    }

    public ArrayList<BasicBlock> getBlocks() {
        ArrayList<BasicBlock> blocks = new ArrayList<>();

        BasicBlock ptr = entranceBlock;
        while (ptr != null) {
            blocks.add(ptr);
            ptr = ptr.getNextBlock();
        }
        return blocks;
    }

    public void addBasicBlock(BasicBlock block) {
        if (entranceBlock == null)
            entranceBlock = block;
        else
            exitBlock.appendBlock(block);
        exitBlock = block;
    }

    public void addBasicBlockNext(BasicBlock block1, BasicBlock block2) {
        // It is ensured that block1 is in this function.
        if (block1 == exitBlock) {
            block2.setNextBlock(null);
            block2.setPrevBlock(block1);
            block1.setNextBlock(block2);
            exitBlock = block2;
        } else {
            block2.setNextBlock(block1.getNextBlock());
            block2.setPrevBlock(block1);
            block1.getNextBlock().setPrevBlock(block2);
            block1.setNextBlock(block2);
        }
    }

    public void splitBlockFromFunction(BasicBlock block) {
        // It is ensured that block is in this function.
        if (block.getPrevBlock() == null)
            entranceBlock = block.getNextBlock();
        else
            block.getPrevBlock().setNextBlock(block.getNextBlock());
        if (block.getNextBlock() == null)
            exitBlock = block.getPrevBlock();
        else
            block.getNextBlock().setPrevBlock(block.getPrevBlock());

        block.setPrevBlock(null);
        block.setNextBlock(null);
    }

    private void dfsBasicBlocks(BasicBlock block, ArrayList<BasicBlock> dfsOrder, Set<BasicBlock> dfsVisit) {
        dfsOrder.add(block);
        dfsVisit.add(block);

        for (BasicBlock successor : block.getSuccessors()) {
            if (!dfsVisit.contains(successor)) {
                dfsBasicBlocks(successor, dfsOrder, dfsVisit);
            }
        }
    }

    public ArrayList<BasicBlock> getDFSOrder() {
        ArrayList<BasicBlock> dfsOrder = new ArrayList<>();
        Set<BasicBlock> dfsVisit = new HashSet<>();
        dfsBasicBlocks(entranceBlock, dfsOrder, dfsVisit);
        return dfsOrder;
    }

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
