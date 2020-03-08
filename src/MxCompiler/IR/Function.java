package MxCompiler.IR;

import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Operand.ConstNull;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.FunctionType;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.VoidType;
import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.ErrorHandler;
import MxCompiler.Utilities.SymbolTable;

import java.util.ArrayList;
import java.util.HashSet;

public class Function extends IRObject {
    private Module module;

    private String name;
    private ArrayList<Parameter> parameters;
    private FunctionType functionType;

    private BasicBlock entranceBlock;
    private BasicBlock exitBlock;
    private BasicBlock returnBlock;
    private Register returnValue;

    private SymbolTable symbolTable; // symbol table of operands

    private boolean external;

    private ArrayList<BasicBlock> dfsOrder;
    private HashSet<BasicBlock> dfsVisit;

    private boolean sideEffect;


    public Function(Module module, String name, IRType returnType,
                    ArrayList<Parameter> parameters, boolean external) {
        this.module = module;
        this.name = name;
        this.parameters = parameters;
        ArrayList<IRType> parameterList = new ArrayList<>();
        for (Parameter parameter : parameters) {
            parameterList.add(parameter.getType());
            parameter.setFunction(this);
        }
        functionType = new FunctionType(returnType, parameterList);

        entranceBlock = null;
        exitBlock = null;
        returnBlock = null;
        returnValue = null;

        symbolTable = new SymbolTable();
        this.external = external;
        sideEffect = true;

        // Add parameters to symbol table.
        for (Parameter parameter : parameters)
            symbolTable.put(parameter.getName(), parameter);
    }

    public Module getModule() {
        return module;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    public FunctionType getFunctionType() {
        return functionType;
    }

    public BasicBlock getEntranceBlock() {
        return entranceBlock;
    }

    public void setEntranceBlock(BasicBlock entranceBlock) {
        this.entranceBlock = entranceBlock;
    }

    public BasicBlock getExitBlock() {
        return exitBlock;
    }

    public void setExitBlock(BasicBlock exitBlock) {
        this.exitBlock = exitBlock;
    }

    public BasicBlock getReturnBlock() {
        return returnBlock;
    }

    public Register getReturnValue() {
        return returnValue;
    }

    public Operand getActualReturnValue() {
        if (exitBlock.getInstTail() instanceof ReturnInst)
            return ((ReturnInst) exitBlock.getInstTail()).getReturnValue();
        else
            return new ConstNull();
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public boolean hasSideEffect() {
        return sideEffect;
    }

    public void setSideEffect(boolean sideEffect) {
        this.sideEffect = sideEffect;
    }

    public void addBasicBlock(BasicBlock block) {
        if (entranceBlock == null)
            entranceBlock = block;
        else
            exitBlock.appendBlock(block);

        exitBlock = block;
    }

    public ArrayList<BasicBlock> getBlocks() {
        ArrayList<BasicBlock> blocks = new ArrayList<>();

        BasicBlock ptr = entranceBlock;
        while (ptr != null) {
            blocks.add(ptr);
            ptr = ptr.getNext();
        }
        return blocks;
    }

    public ArrayList<AllocateInst> getAllocaInstructions() {
        ArrayList<AllocateInst> allocaInst = new ArrayList<>();
        IRInstruction ptr = entranceBlock.getInstHead();
        while (ptr != null) {
            if (ptr instanceof AllocateInst)
                allocaInst.add((AllocateInst) ptr);
            ptr = ptr.getInstNext();
        }
        return allocaInst;
    }

    public void initialize() {
        BasicBlock block = new BasicBlock(this, "entranceBlock"); // It becomes the entrance block.
        addBasicBlock(block);
        returnBlock = new BasicBlock(this, "returnBlock");
        symbolTable.put(entranceBlock.getName(), entranceBlock);
        symbolTable.put(returnBlock.getName(), returnBlock);

        IRType returnType = functionType.getReturnType();
        if (returnType instanceof VoidType)
            returnBlock.addInstruction(new ReturnInst(returnBlock, new VoidType(), null));
        else {
            returnValue = new Register(new PointerType(returnType), "returnValue$addr");
            entranceBlock.addInstruction(new AllocateInst(entranceBlock, returnValue, returnType));
            entranceBlock.addInstruction(new StoreInst(entranceBlock, returnType.getDefaultValue(), returnValue));
            Register loadReturnValue = new Register(returnType, "returnValue");
            returnBlock.addInstruction(new LoadInst(returnBlock, returnType, returnValue, loadReturnValue));
            returnBlock.addInstruction(new ReturnInst(returnBlock, returnType, loadReturnValue));

            symbolTable.put(returnValue.getName(), returnValue);
            symbolTable.put(loadReturnValue.getName(), loadReturnValue);
        }
    }

    public String declareToString() {
        StringBuilder string = new StringBuilder("declare ");
        string.append(functionType.getReturnType().toString());
        string.append(" @").append(name);

        string.append("(");
        for (int i = 0; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            string.append(parameter.getType().toString()).append(" ");
            string.append(parameter.toString());
            if (i != parameters.size() - 1)
                string.append(", ");
        }
        string.append(")");

        return string.toString();
    }

    public void checkBlockTerminalInst(ErrorHandler errorHandler) throws CompilationError {
        ArrayList<BasicBlock> blocks = getBlocks();
        for (BasicBlock block : blocks) {
            if (block.notEndWithTerminalInst()) {
                errorHandler.error("Function \"" + name + "\" has no return statement.");
                throw new CompilationError();
            }
        }
    }

    private void dfsBasicBlocks(BasicBlock block) {
        block.setDfn(dfsOrder.size());
        dfsOrder.add(block);
        dfsVisit.add(block);

        for (BasicBlock successor : block.getSuccessors())
            if (!dfsVisit.contains(successor)) {
                successor.setDfsFather(block);
                dfsBasicBlocks(successor);
            }
    }

    public ArrayList<BasicBlock> getDFSOrder() {
        dfsOrder = new ArrayList<>();
        dfsVisit = new HashSet<>();
        entranceBlock.setDfsFather(null);
        dfsBasicBlocks(entranceBlock);
        return dfsOrder;
    }

    public boolean isNotFunctional() {
        int returnInstCnt = 0;
        for (BasicBlock block : getBlocks()) {
            if (block.notEndWithTerminalInst())
                return true;
            if (block.getInstTail() instanceof ReturnInst)
                returnInstCnt++;
        }
        if (!(exitBlock.getInstTail() instanceof ReturnInst))
            return false;
        return returnInstCnt != 1;
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
