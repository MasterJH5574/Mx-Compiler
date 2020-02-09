package MxCompiler.IR;

import MxCompiler.AST.*;
import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.Frontend.Scope;
import MxCompiler.IR.Instruction.AllocateInst;
import MxCompiler.IR.Instruction.StoreInst;
import MxCompiler.IR.Operand.GlobalVariable;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.*;
import MxCompiler.IR.TypeSystem.VoidType;
import MxCompiler.Type.*;
import MxCompiler.Utilities.CompilationError;

import java.util.ArrayList;

public class IRBuilder implements ASTVisitor {
    private Module module;

    private Scope globalScope;
    private TypeTable astTypeTable;
    private IRTypeTable irTypeTable;

    private Function currentFunction;
    private BasicBlock currentBlock;
    private BasicBlock loopBlock;
    private BasicBlock loopExitBlock;

    private Function initializer;

    public IRBuilder(Scope globalScope, TypeTable astTypeTable) {
        module = new Module(astTypeTable);

        this.globalScope = globalScope;
        this.astTypeTable = astTypeTable;
        this.irTypeTable = module.getIrTypeTable();

        currentFunction = null;
        currentBlock = null;
        loopBlock = null;
        loopExitBlock = null;

        initializer = new Function(module, "__initialize", new VoidType(), new ArrayList<>());
        initializer.initialize();
        module.addFunction(initializer);
    }



    @Override
    public void visit(ProgramNode node) throws CompilationError {
        // ------ set function initializer ------
        currentFunction = initializer;
        currentBlock = initializer.getEntranceBlock();
        for (ProgramUnitNode unit : node.getProgramUnits()) // Step 1: declare global variables
            if (unit instanceof VarNode)
                unit.accept(this);

        currentFunction = null;
        currentBlock = null;
        // ------  set initializer finish  ------

        for (ProgramUnitNode unit : node.getProgramUnits()) // Step 2: define classes
            if (unit instanceof ClassNode)
                unit.accept(this);

        for (ProgramUnitNode unit : node.getProgramUnits()) // Step 3: define functions
            if (unit instanceof FunctionNode)
                unit.accept(this);
    }

    @Override
    public void visit(PrimitiveTypeNode node) throws CompilationError {
        // This method will never be called.
    }

    @Override
    public void visit(ClassTypeNode node) throws CompilationError {
        // This method will never be called.
    }

    @Override
    public void visit(ArrayTypeNode node) throws CompilationError {
        // This method will never be called.
    }

    @Override
    public void visit(VarNodeList node) throws CompilationError {
        // This method will never be called.
    }

    @Override
    public void visit(VarNode node) throws CompilationError {
        Type type = astTypeTable.get(node.getType());
        IRType irType = type.getIRType(irTypeTable);
        String name = node.getIdentifier();
        if (node.getScope() == globalScope) { // global variables
            GlobalVariable globalVariable = new GlobalVariable(irType, name, null);
            Operand init;
            if (node.hasInitExpr()) {
                ExprNode initExpr = node.getInitExpr();
                initExpr.accept(this); // visit ExprNode, add instructions to initializer.
                init = initExpr.getResult();
                if (!init.isConstValue()) {
                    currentBlock.addInstruction(new StoreInst(currentBlock, init, globalVariable));
                    init = null;
                }
            } else
                init = type.getDefaultValue();
            globalVariable.setInit(init);
            module.addGlobalVariable(globalVariable);
        } else { // local variables
            VariableEntity variableEntity = (VariableEntity) node.getScope().getEntity(name);
            Register allocaAddr = new Register(irType, name + "#addr");
            BasicBlock entranceBlock = currentFunction.getEntranceBlock();
            entranceBlock.addInstructionAtFront(new AllocateInst(entranceBlock, allocaAddr, irType));
            currentFunction.getSymbolTable().put(allocaAddr.getName(), allocaAddr);
            variableEntity.setAllocaAddr(allocaAddr);
        }
    }

    @Override
    public void visit(FunctionNode node) throws CompilationError {
        String functionName;
        IRType returnType;
        ArrayList<Parameter> parameters = new ArrayList<>();
        FunctionEntity functionEntity;
        if (node.getScope().inClassScope()) { // constructor or method
            ClassType classType = (ClassType) node.getScope().getClassType();
            parameters.add(new Parameter(classType.getIRType(irTypeTable), "this"));

            String className = classType.getName();
            String methodName = node.getIdentifier();
            functionName = className + "." + methodName;
            functionEntity = (FunctionEntity) node.getScope().getEntity(methodName);
        } else {
            functionName = node.getIdentifier();
            functionEntity = (FunctionEntity) node.getScope().getEntity(functionName);
        }
        returnType = astTypeTable.get(functionEntity.getReturnType()).getIRType(irTypeTable);
        ArrayList<VariableEntity> entityParameters = functionEntity.getParameters();
        for (VariableEntity entityParameter : entityParameters)
            parameters.add(new Parameter(astTypeTable.get(entityParameter.getType()).getIRType(irTypeTable),
                    entityParameter.getName()));
        Function function = new Function(module, functionName, returnType, parameters);
        module.addFunction(function);
        function.initialize();

        currentFunction = function;
        currentBlock = function.getEntranceBlock();

        // Add alloca and store to parameters.
        int offset = node.getScope().inClassScope() ? 1 : 0;
        for (int i = 0; i < entityParameters.size(); i++) {
            Parameter parameter = parameters.get(i + offset);
            Register allocaAddr = new Register(new PointerType(parameter.getType()),
                    parameter.getNameWithoutDot() + "#addr");
            currentBlock.addInstruction(new AllocateInst(currentBlock, allocaAddr, parameter.getType()));
            currentBlock.addInstruction(new StoreInst(currentBlock, parameter, allocaAddr));
            function.getSymbolTable().put(allocaAddr.getName(), allocaAddr);
            entityParameters.get(i).setAllocaAddr(allocaAddr);
        }

        node.getStatement().accept(this); // visit StmtNode

        currentFunction = null;
        currentBlock = null;
    }

    @Override
    public void visit(ClassNode node) throws CompilationError {
        if (node.hasConstructor())
            node.getConstructor().accept(this); // visit FunctionNode
        for (FunctionNode method : node.getFuncList())
            method.accept(this); // visit FunctionNode
    }

    @Override
    public void visit(BlockNode node) throws CompilationError {
        ArrayList<StmtNode> statements = node.getStatements();
        for (StmtNode statement : statements)
            statement.accept(this); // visit StmtNode
    }

    @Override
    public void visit(VarDeclStmtNode node) throws CompilationError {
        for (VarNode variable : node.getVarList())
            variable.accept(this); // visit VarNode
    }

    @Override
    public void visit(IfStmtNode node) throws CompilationError {
        // Todo
    }

    @Override
    public void visit(WhileStmtNode node) throws CompilationError {

    }

    @Override
    public void visit(ForStmtNode node) throws CompilationError {

    }

    @Override
    public void visit(ReturnStmtNode node) throws CompilationError {

    }

    @Override
    public void visit(BreakStmtNode node) throws CompilationError {

    }

    @Override
    public void visit(ContinueStmtNode node) throws CompilationError {

    }

    @Override
    public void visit(ExprStmtNode node) throws CompilationError {

    }

    @Override
    public void visit(PostfixExprNode node) throws CompilationError {

    }

    @Override
    public void visit(PrefixExprNode node) throws CompilationError {

    }

    @Override
    public void visit(BinaryExprNode node) throws CompilationError {

    }

    @Override
    public void visit(NewExprNode node) throws CompilationError {

    }

    @Override
    public void visit(MemberExprNode node) throws CompilationError {

    }

    @Override
    public void visit(FuncCallExprNode node) throws CompilationError {

    }

    @Override
    public void visit(SubscriptExprNode node) throws CompilationError {

    }

    @Override
    public void visit(ThisExprNode node) throws CompilationError {

    }

    @Override
    public void visit(IdExprNode node) throws CompilationError {

    }

    @Override
    public void visit(BoolLiteralNode node) {

    }

    @Override
    public void visit(IntLiteralNode node) {

    }

    @Override
    public void visit(StringLiteralNode node) {

    }

    @Override
    public void visit(NullLiteralNode node) {

    }
}
