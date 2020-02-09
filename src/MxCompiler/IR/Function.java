package MxCompiler.IR;

import MxCompiler.IR.Instruction.AllocateInst;
import MxCompiler.IR.Instruction.LoadInst;
import MxCompiler.IR.Instruction.ReturnInst;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.FunctionType;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.IR.TypeSystem.VoidType;
import MxCompiler.Utilities.SymbolTable;

import java.util.ArrayList;

public class Function {
    private Module module;

    private String name;
    private ArrayList<Parameter> parameters;
    private FunctionType functionType;

    private BasicBlock entranceBlock;
    private BasicBlock exitBlock;
    private BasicBlock returnBlock;
    private Register returnValue;

    private SymbolTable symbolTable; // symbol table of operands


    private boolean define;

    public Function(Module module, String name, IRType returnType, ArrayList<Parameter> parameters) {
        this.module = module;
        this.name = name;
        this.parameters = parameters;
        ArrayList<IRType> parameterList = new ArrayList<>();
        for (Parameter parameter : parameters)
            parameterList.add(parameter.getType());
        functionType = new FunctionType(returnType, parameterList);

        entranceBlock = null;
        exitBlock = null;
        returnBlock = null;
        returnValue = null;

        symbolTable = new SymbolTable();
        define = true;

        // Add parameters to symbol table.
        for (Parameter parameter : parameters)
            symbolTable.put(parameter.getName(), parameter);
    }

    public String getName() {
        return name;
    }

    public ArrayList<Parameter> getParameters() {
        return parameters;
    }

    public BasicBlock getEntranceBlock() {
        return entranceBlock;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public BasicBlock addBasicBlock(String name) {
        BasicBlock block = new BasicBlock(this, name);
        if (entranceBlock == null)
            entranceBlock = block;
        else
            exitBlock.appendBlock(block);

        exitBlock = block;
        symbolTable.put(name, block);
        return block;
    }

    public void initialize() {
        addBasicBlock("entrance"); // It becomes the entrance block.
        returnBlock = new BasicBlock(this, "returnBlock");

        IRType returnType = functionType.getReturnType();
        if (returnType instanceof VoidType)
            returnBlock.addInstruction(new ReturnInst(returnBlock, new VoidType(), null));
        else {
            returnValue = new Register(new PointerType(returnType), "returnValue");
            entranceBlock.addInstruction(new AllocateInst(entranceBlock, returnValue, returnType));
            Register loadReturnValue = new Register(returnType, "loadReturnValue");
            returnBlock.addInstruction(new LoadInst(returnBlock, returnType, returnValue, loadReturnValue));
            returnBlock.addInstruction(new ReturnInst(returnBlock, returnType, returnValue));

            symbolTable.put(returnValue.getName(), returnValue);
            symbolTable.put(loadReturnValue.getName(), loadReturnValue);
        }
    }
}
