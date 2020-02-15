package MxCompiler.AST;

import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.AllocateInst;
import MxCompiler.IR.Instruction.StoreInst;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IRTypeTable;
import MxCompiler.IR.TypeSystem.PointerType;
import MxCompiler.Type.ClassType;
import MxCompiler.Type.TypeTable;
import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class FunctionNode extends ProgramUnitNode {
    private TypeNode type;
    private String identifier;
    private ArrayList<VarNode> parameters;
    private StmtNode statement; // This must be a block statement.

    public FunctionNode(Location location, TypeNode type, String identifier,
                        ArrayList<VarNode> parameters, StmtNode statement) {
        super(location);
        this.type = type;
        this.identifier = identifier;
        this.parameters = parameters;
        this.statement = statement;
    }

    public TypeNode getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ArrayList<VarNode> getParameters() {
        return parameters;
    }

    public void addParameter(VarNode parameter) {
        parameters.add(parameter);
    }

    public StmtNode getStatement() {
        return statement;
    }

    public FunctionEntity getEntity(FunctionEntity.EntityType entityType) {
        ArrayList<VariableEntity> parameters = new ArrayList<>();
        for (VarNode varNode : this.parameters)
            parameters.add(varNode.getEntity(VariableEntity.EntityType.parameter));
        return new FunctionEntity(identifier, getLocation(), type, parameters, statement, entityType);
    }

    public void addFunctionToModule(Module module, TypeTable astTypeTable, IRTypeTable irTypeTable) {
        String functionName;
        IRType returnType;
        ArrayList<Parameter> parameters = new ArrayList<>();
        FunctionEntity functionEntity;
        if (getScope().inClassScope()) { // constructor or method
            ClassType classType = (ClassType) getScope().getClassType();
            parameters.add(new Parameter(classType.getIRType(irTypeTable), "this"));

            String className = classType.getName();
            String methodName = getIdentifier();
            functionName = className + "." + methodName;
            functionEntity = getScope().getFunctionEntityIncludeConstructor(methodName);
        } else {
            functionName = getIdentifier();
            functionEntity = getScope().getFunctionEntity(functionName);
        }
        returnType = astTypeTable.get(functionEntity.getReturnType()).getIRType(irTypeTable);
        ArrayList<VariableEntity> entityParameters = functionEntity.getParameters();
        for (VariableEntity entityParameter : entityParameters)
            parameters.add(new Parameter(astTypeTable.get(entityParameter.getType()).getIRType(irTypeTable),
                    entityParameter.getName()));
        Function function = new Function(module, functionName, returnType, parameters, false);
        module.addFunction(function);
        function.initialize();

        BasicBlock currentBlock = function.getEntranceBlock();

        // Add alloca and store to parameters.
        if (getScope().inClassScope()) {
            Parameter parameter = parameters.get(0);
            Register allocaAddr = new Register(new PointerType(parameter.getType()),
                    "this$addr");
            currentBlock.addInstruction(new AllocateInst(currentBlock, allocaAddr, parameter.getType()));
            currentBlock.addInstruction(new StoreInst(currentBlock, parameter, allocaAddr));
            function.getSymbolTable().put(allocaAddr.getName(), allocaAddr);
        }
        int offset = getScope().inClassScope() ? 1 : 0;
        for (int i = 0; i < entityParameters.size(); i++) {
            Parameter parameter = parameters.get(i + offset);
            Register allocaAddr = new Register(new PointerType(parameter.getType()),
                    parameter.getNameWithoutDot() + "$addr");
            currentBlock.addInstruction(new AllocateInst(currentBlock, allocaAddr, parameter.getType()));
            currentBlock.addInstruction(new StoreInst(currentBlock, parameter, allocaAddr));
            function.getSymbolTable().put(allocaAddr.getName(), allocaAddr);
            entityParameters.get(i).setAllocaAddr(allocaAddr);
        }
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("<FunctionNode>\n");
        string.append("returnType:\n").append(type.toString());
        string.append("identifier = ").append(identifier);
        string.append("parameter:\n");
        for (VarNode parameter : parameters)
            string.append(parameter.toString());
        string.append("statements:\n").append(statement.toString());
        return string.toString();
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }
}
