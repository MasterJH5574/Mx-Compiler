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
import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.ErrorHandler;
import MxCompiler.Utilities.SymbolTable;

import java.util.ArrayList;

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

    public FunctionType getFunctionType() {
        return functionType;
    }

    public BasicBlock getEntranceBlock() {
        return entranceBlock;
    }

    public BasicBlock getExitBlock() {
        return exitBlock;
    }

    public BasicBlock getReturnBlock() {
        return returnBlock;
    }

    public Register getReturnValue() {
        return returnValue;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public void addBasicBlock(BasicBlock block) {
        if (entranceBlock == null)
            entranceBlock = block;
        else
            exitBlock.appendBlock(block);

        exitBlock = block;
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
            returnValue = new Register(new PointerType(returnType), "returnValue");
            entranceBlock.addInstruction(new AllocateInst(entranceBlock, returnValue, returnType));
            Register loadReturnValue = new Register(returnType, "loadReturnValue");
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
        BasicBlock ptr = entranceBlock;
        while (ptr != null) {
            if (!ptr.endWithTerminalInst()) {
                errorHandler.error("Function \"" + name + "\" has no return statement.");
                throw new CompilationError();
            }
            ptr = ptr.getNext();
        }
    }

    public void accept(IRVisitor visitor) {
        visitor.visit(this);
    }
}
