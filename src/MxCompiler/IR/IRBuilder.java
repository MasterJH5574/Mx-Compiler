package MxCompiler.IR;

import MxCompiler.AST.*;
import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.Frontend.Scope;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.*;
import MxCompiler.IR.TypeSystem.VoidType;
import MxCompiler.Type.*;
import MxCompiler.Type.ArrayType;
import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.ErrorHandler;
import MxCompiler.Utilities.Pair;

import java.util.ArrayList;
import java.util.Stack;

public class IRBuilder implements ASTVisitor {
    private Module module;

    private Scope globalScope;
    private TypeTable astTypeTable;
    private IRTypeTable irTypeTable;

    private Function currentFunction;
    private BasicBlock currentBlock;

    private Stack<BasicBlock> loopBreakBlock;
    private Stack<BasicBlock> loopContinueBlock;

    private Function initializer;

    private ErrorHandler errorHandler;

    public IRBuilder(Scope globalScope, TypeTable astTypeTable, ErrorHandler errorHandler) {
        module = new Module(astTypeTable);

        this.globalScope = globalScope;
        this.astTypeTable = astTypeTable;
        this.irTypeTable = module.getIrTypeTable();

        currentFunction = null;
        currentBlock = null;
        loopBreakBlock = new Stack<>();
        loopContinueBlock = new Stack<>();

        initializer = new Function(module, "__init__", new VoidType(), new ArrayList<>(), false);
        initializer.initialize();
        module.addFunction(initializer);

        this.errorHandler = errorHandler;
    }

    public Module getModule() {
        return module;
    }

    public Function getCurrentFunction() {
        return currentFunction;
    }

    public BasicBlock getCurrentBlock() {
        return currentBlock;
    }

    public void setCurrentBlock(BasicBlock currentBlock) {
        this.currentBlock = currentBlock;
    }

    @Override
    public void visit(ProgramNode node) throws CompilationError {
        for (ProgramUnitNode unit : node.getProgramUnits()) // Step 1: add class methods
            if (unit instanceof ClassNode) {
                if (((ClassNode) unit).hasConstructor())
                    ((ClassNode) unit).getConstructor().addFunctionToModule(module, astTypeTable, irTypeTable);
                for (FunctionNode method : ((ClassNode) unit).getFuncList())
                    method.addFunctionToModule(module, astTypeTable, irTypeTable);
            }

        for (ProgramUnitNode unit : node.getProgramUnits()) // Step 2: add functions
            if (unit instanceof FunctionNode)
                ((FunctionNode) unit).addFunctionToModule(module, astTypeTable, irTypeTable);


        // ------ set function initializer ------
        currentFunction = initializer;
        currentBlock = initializer.getEntranceBlock();
        for (ProgramUnitNode unit : node.getProgramUnits()) // Step 3: declare global variables
            if (unit instanceof VarNode)
                unit.accept(this);
        currentBlock.addInstruction(new BranchInst(currentBlock,
                null, currentFunction.getReturnBlock(), null));
        currentFunction.addBasicBlock(currentFunction.getReturnBlock());

        currentFunction = null;
        currentBlock = null;
        // ------  set initializer finish  ------


        for (ProgramUnitNode unit : node.getProgramUnits()) // Step 4: define classes
            if (unit instanceof ClassNode)
                unit.accept(this);

        for (ProgramUnitNode unit : node.getProgramUnits()) // Step 5: define functions
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
        VariableEntity variableEntity = (VariableEntity) node.getScope().getEntity(name);
        if (node.getScope() == globalScope) { // global variables
            GlobalVariable globalVariable = new GlobalVariable(irType, name, null);
            Operand init;
            if (node.hasInitExpr()) {
                ExprNode initExpr = node.getInitExpr();
                initExpr.accept(this); // visit ExprNode, add instructions to initializer.
                init = initExpr.getResult();
                if (!init.isConstValue()) {
                    currentBlock.addInstruction(new StoreInst(currentBlock, init, globalVariable));
                    init = type.getDefaultValue();
                }
            } else
                init = type.getDefaultValue();
            globalVariable.setInit(init);
            module.addGlobalVariable(globalVariable);
            variableEntity.setAllocaAddr(globalVariable);
        } else { // local variables
            Register allocaAddr = new Register(new PointerType(irType), name + "$addr");
            BasicBlock entranceBlock = currentFunction.getEntranceBlock();
            entranceBlock.addInstructionAtFront(new AllocateInst(entranceBlock, allocaAddr, irType));
            currentFunction.getSymbolTable().put(allocaAddr.getName(), allocaAddr);
            variableEntity.setAllocaAddr(allocaAddr);

            if (node.hasInitExpr()) {
                Operand init;
                ExprNode initExpr = node.getInitExpr();
                initExpr.accept(this); // visit ExprNode
                init = initExpr.getResult();
                currentBlock.addInstruction(new StoreInst(currentBlock, init, allocaAddr));
            }
        }
    }

    @Override
    public void visit(FunctionNode node) throws CompilationError {
        String functionName;
        if (node.getScope().inClassScope()) { // constructor or method
            ClassType classType = (ClassType) node.getScope().getClassType();

            String className = classType.getName();
            String methodName = node.getIdentifier();
            functionName = className + "." + methodName;
        } else {
            functionName = node.getIdentifier();
        }

        assert module.getFunctionMap().containsKey(functionName);
        Function function = module.getFunctionMap().get(functionName);

        currentFunction = function;
        currentBlock = function.getEntranceBlock();

        node.getStatement().accept(this); // visit StmtNode

        currentBlock.addInstruction(new BranchInst(currentBlock,
                null, currentFunction.getReturnBlock(), null));
        function.addBasicBlock(function.getReturnBlock());

        // Check if there is enough return statement.
        function.checkBlockTerminalInst(errorHandler);

        if (node.getIdentifier().equals("main")) {
            function = module.getFunctionMap().get("__init__");
            function.checkBlockTerminalInst(errorHandler);
            currentFunction.getEntranceBlock().addInstructionAtFront(new CallInst(currentBlock, function,
                    new ArrayList<>(), null));
        }

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
        BasicBlock thenBlock = new BasicBlock(currentFunction, "ifThenBlock");
        BasicBlock elseBlock = node.hasElseBody() ? new BasicBlock(currentFunction, "ifElseBlock") : null;
        BasicBlock mergeBlock = new BasicBlock(currentFunction, "ifMergeBlock");

        node.getCond().accept(this); // visit ExprNode
        Operand condResult = node.getCond().getResult();

        if (node.hasElseBody())
            currentBlock.addInstruction(new BranchInst(currentBlock, condResult, thenBlock, elseBlock));
        else
            currentBlock.addInstruction(new BranchInst(currentBlock, condResult, thenBlock, mergeBlock));

        currentBlock = thenBlock;
        node.getThenBody().accept(this); // visit StmtNode
        currentBlock.addInstruction(new BranchInst(currentBlock, null, mergeBlock, null));
        currentFunction.addBasicBlock(thenBlock);

        if (node.hasElseBody()) {
            currentBlock = elseBlock;
            node.getElseBody().accept(this); // visit StmtNode
            currentBlock.addInstruction(new BranchInst(currentBlock, null, mergeBlock, null));
            currentFunction.addBasicBlock(elseBlock);
        }

        currentBlock = mergeBlock;
        currentFunction.addBasicBlock(mergeBlock);


        currentFunction.getSymbolTable().put(thenBlock.getName(), thenBlock);
        if (node.hasElseBody()) {
            assert elseBlock != null;
            currentFunction.getSymbolTable().put(elseBlock.getName(), elseBlock);
        }
        currentFunction.getSymbolTable().put(mergeBlock.getName(), mergeBlock);
    }

    @Override
    public void visit(WhileStmtNode node) throws CompilationError {
        BasicBlock condBlock = new BasicBlock(currentFunction, "whileCondBlock");
        BasicBlock bodyBlock = new BasicBlock(currentFunction, "whileBodyBlock");
        BasicBlock mergeBlock = new BasicBlock(currentFunction, "whileMergeBlock");

        currentBlock.addInstruction(new BranchInst(currentBlock, null, condBlock, null));

        currentBlock = condBlock;
        node.getCond().accept(this); // visit ExprNode
        Operand condResult = node.getCond().getResult();
        currentBlock.addInstruction(new BranchInst(currentBlock, condResult, bodyBlock, mergeBlock));
        currentFunction.addBasicBlock(condBlock);

        loopBreakBlock.push(mergeBlock);
        loopContinueBlock.push(bodyBlock);
        currentBlock = bodyBlock;
        node.getBody().accept(this); // visit StmtNode
        currentBlock.addInstruction(new BranchInst(currentBlock, null, condBlock, null));
        currentFunction.addBasicBlock(bodyBlock);

        loopBreakBlock.pop();
        loopContinueBlock.pop();

        currentBlock = mergeBlock;
        currentFunction.addBasicBlock(mergeBlock);


        currentFunction.getSymbolTable().put(condBlock.getName(), condBlock);
        currentFunction.getSymbolTable().put(bodyBlock.getName(), bodyBlock);
        currentFunction.getSymbolTable().put(mergeBlock.getName(), mergeBlock);
    }

    @Override
    public void visit(ForStmtNode node) throws CompilationError {
        BasicBlock condBlock = node.hasCond() ? new BasicBlock(currentFunction, "forCondBlock") : null;
        BasicBlock stepBlock = node.hasStep() ? new BasicBlock(currentFunction, "forStepBlock") : null;
        BasicBlock bodyBlock = new BasicBlock(currentFunction, "forBodyBlock");
        BasicBlock mergeBlock = new BasicBlock(currentFunction, "forMergeBlock");

        if (node.hasInit())
            node.getInit().accept(this); // visit ExprNode

        if (node.hasCond()) {
            currentBlock.addInstruction(new BranchInst(currentBlock, null, condBlock, null));

            currentBlock = condBlock;
            node.getCond().accept(this); // visit ExprNode
            Operand condResult = node.getCond().getResult();
            currentBlock.addInstruction(new BranchInst(currentBlock, condResult, bodyBlock, mergeBlock));
            currentFunction.addBasicBlock(condBlock);

            loopBreakBlock.push(mergeBlock);
            loopContinueBlock.push(node.hasStep() ? stepBlock : condBlock);
            currentBlock = bodyBlock;
            node.getBody().accept(this); // visit StmtNode
            if (node.hasStep())
                currentBlock.addInstruction(new BranchInst(currentBlock, null, stepBlock, null));
            else
                currentBlock.addInstruction(new BranchInst(currentBlock, null, condBlock, null));
            currentFunction.addBasicBlock(bodyBlock);

            loopBreakBlock.pop();
            loopContinueBlock.pop();

            if (node.hasStep()) {
                currentBlock = stepBlock;
                node.getStep().accept(this); // visit ExprNode
                currentBlock.addInstruction(new BranchInst(currentBlock, null, condBlock, null));
                currentFunction.addBasicBlock(stepBlock);
            }
        } else {
            currentBlock.addInstruction(new BranchInst(currentBlock, null, bodyBlock, null));

            loopBreakBlock.push(mergeBlock);
            loopContinueBlock.push(node.hasStep() ? stepBlock : condBlock);
            currentBlock = bodyBlock;
            node.getBody().accept(this); // visit StmtNode
            if (node.hasStep())
                currentBlock.addInstruction(new BranchInst(currentBlock, null, stepBlock, null));
            else
                currentBlock.addInstruction(new BranchInst(currentBlock, null, bodyBlock, null));
            currentFunction.addBasicBlock(bodyBlock);

            loopBreakBlock.pop();
            loopContinueBlock.pop();

            if (node.hasStep()) {
                currentBlock = stepBlock;
                node.getStep().accept(this); // visit ExprNode
                currentBlock.addInstruction(new BranchInst(currentBlock, null, bodyBlock, null));
                currentFunction.addBasicBlock(stepBlock);
            }
        }

        currentBlock = mergeBlock;
        currentFunction.addBasicBlock(mergeBlock);


        if (node.hasCond()) {
            assert condBlock != null;
            currentFunction.getSymbolTable().put(condBlock.getName(), condBlock);
        }
        if (node.hasStep()) {
            assert stepBlock != null;
            currentFunction.getSymbolTable().put(stepBlock.getName(), stepBlock);
        }
        currentFunction.getSymbolTable().put(bodyBlock.getName(), bodyBlock);
        currentFunction.getSymbolTable().put(mergeBlock.getName(), mergeBlock);
    }

    @Override
    public void visit(ReturnStmtNode node) throws CompilationError {
        if (node.hasReturnValue()) {
            node.getReturnValue().accept(this); // visit ExprNode
            Operand result = node.getReturnValue().getResult();
            currentBlock.addInstruction(new StoreInst(currentBlock, result, currentFunction.getReturnValue()));
        }
        currentBlock.addInstruction(new BranchInst(currentBlock,
                null, currentFunction.getReturnBlock(), null));
    }

    @Override
    public void visit(BreakStmtNode node) throws CompilationError {
        currentBlock.addInstruction(new BranchInst(currentBlock, null, loopBreakBlock.peek(), null));
    }

    @Override
    public void visit(ContinueStmtNode node) throws CompilationError {
        currentBlock.addInstruction(new BranchInst(currentBlock,
                null, loopContinueBlock.peek(), null));
    }

    @Override
    public void visit(ExprStmtNode node) throws CompilationError {
        node.getExpr().accept(this); // visit ExprNode
    }

    @Override
    public void visit(PostfixExprNode node) throws CompilationError {
        node.getExpr().accept(this); // visit ExprNode

        Register result;
        Operand exprResult = node.getExpr().getResult();
        Operand addr = node.getExpr().getLvalueResult();
        if (node.getOp() == PostfixExprNode.Operator.postInc) {
            // a++
            result = new Register(new IntegerType(IntegerType.BitWidth.int32), "postInc");
            currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.add,
                    exprResult, new ConstInt(1), result));
        } else {
            // a--
            result = new Register(new IntegerType(IntegerType.BitWidth.int32), "postDec");
            currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.sub,
                    exprResult, new ConstInt(1), result));
        }
        currentBlock.addInstruction(new StoreInst(currentBlock, result, addr));

        node.setResult(exprResult);
        node.setLvalueResult(null);
        currentFunction.getSymbolTable().put(result.getName(), result);
    }

    @Override
    public void visit(PrefixExprNode node) throws CompilationError {
        node.getExpr().accept(this); // visit ExprNode

        PrefixExprNode.Operator op = node.getOp();
        Operand exprResult = node.getExpr().getResult();
        if (op == PrefixExprNode.Operator.preInc) {
            // ++a
            Operand addr = node.getExpr().getLvalueResult();
            Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "preInc");
            currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.add,
                    exprResult, new ConstInt(1), result));
            currentBlock.addInstruction(new StoreInst(currentBlock, result, addr));

            node.setResult(result);
            node.setLvalueResult(addr);
            currentFunction.getSymbolTable().put(result.getName(), result);
        } else if (op == PrefixExprNode.Operator.preDec) {
            // --a
            Operand addr = node.getExpr().getLvalueResult();
            Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "preDec");
            currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.sub,
                    exprResult, new ConstInt(1), result));
            currentBlock.addInstruction(new StoreInst(currentBlock, result, addr));

            node.setResult(result);
            node.setLvalueResult(addr);
            currentFunction.getSymbolTable().put(result.getName(), result);
        } else if (op == PrefixExprNode.Operator.signPos) {
            // +a
            node.setResult(exprResult);
            node.setLvalueResult(null);
        } else if (op == PrefixExprNode.Operator.signNeg) {
            // -a
            if (exprResult.isConstValue()) {
                assert exprResult instanceof ConstInt;
                node.setResult(new ConstInt(-((ConstInt) exprResult).getValue()));
                node.setLvalueResult(null);
            } else {
                Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "signNeg");
                currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.sub,
                        new ConstInt(0), exprResult, result));

                node.setResult(result);
                node.setLvalueResult(null);
                currentFunction.getSymbolTable().put(result.getName(), result);
            }
        } else if (op == PrefixExprNode.Operator.logicalNot) {
            // !a
            Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "logicalNot");
            currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.xor,
                    new ConstBool(true), exprResult, result));

            node.setResult(result);
            node.setLvalueResult(null);
            currentFunction.getSymbolTable().put(result.getName(), result);
        } else {
            // ~a
            Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "bitwiseComplement");
            currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.xor,
                    new ConstInt(-1), exprResult, result));

            node.setResult(result);
            node.setLvalueResult(null);
            currentFunction.getSymbolTable().put(result.getName(), result);
        }
    }

    @Override
    public void visit(BinaryExprNode node) throws CompilationError {
        BinaryExprNode.Operator op = node.getOp();
        if (op != BinaryExprNode.Operator.logicalAnd && op != BinaryExprNode.Operator.logicalOr) {
            node.getLhs().accept(this); // visit ExprNode
            node.getRhs().accept(this); // visit ExprNode

            Operand lhsResult = node.getLhs().getResult();
            Operand rhsResult = node.getRhs().getResult();
            // Handle operators according to the order in Checker.
            if (op == BinaryExprNode.Operator.mul) {
                // a * b  for int
                Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "mul");
                currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.mul,
                        lhsResult, rhsResult, result));

                node.setResult(result);
                node.setLvalueResult(null);
                currentFunction.getSymbolTable().put(result.getName(), result);
            } else if (op == BinaryExprNode.Operator.div) {
                // a / b  for int
                Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "div");
                currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.sdiv,
                        lhsResult, rhsResult, result));

                node.setResult(result);
                node.setLvalueResult(null);
                currentFunction.getSymbolTable().put(result.getName(), result);
            } else if (op == BinaryExprNode.Operator.mod) {
                // a % b  for int
                Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "mod");
                currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.srem,
                        lhsResult, rhsResult, result));

                node.setResult(result);
                node.setLvalueResult(null);
                currentFunction.getSymbolTable().put(result.getName(), result);
            } else if (op == BinaryExprNode.Operator.sub) {
                // a - b  for int
                Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "sub");
                currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.sub,
                        lhsResult, rhsResult, result));

                node.setResult(result);
                node.setLvalueResult(null);
                currentFunction.getSymbolTable().put(result.getName(), result);
            } else if (op == BinaryExprNode.Operator.shiftLeft) {
                // a << b  for int
                Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "shiftLeft");
                currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.shl,
                        lhsResult, rhsResult, result));

                node.setResult(result);
                node.setLvalueResult(null);
                currentFunction.getSymbolTable().put(result.getName(), result);
            } else if (op == BinaryExprNode.Operator.shiftRight) {
                // a >> b  for int
                Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "shiftRight");
                currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.ashr,
                        lhsResult, rhsResult, result));

                node.setResult(result);
                node.setLvalueResult(null);
                currentFunction.getSymbolTable().put(result.getName(), result);
            } else if (op == BinaryExprNode.Operator.bitwiseAnd) {
                // a & b  for int
                Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "bitwiseAnd");
                currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.and,
                        lhsResult, rhsResult, result));

                node.setResult(result);
                node.setLvalueResult(null);
                currentFunction.getSymbolTable().put(result.getName(), result);
            } else if (op == BinaryExprNode.Operator.bitwiseXor) {
                // a ^ b  for int
                Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "bitwiseXor");
                currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.xor,
                        lhsResult, rhsResult, result));

                node.setResult(result);
                node.setLvalueResult(null);
                currentFunction.getSymbolTable().put(result.getName(), result);
            } else if (op == BinaryExprNode.Operator.bitwiseOr) {
                // a | b  for int
                Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "bitwiseOr");
                currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.or,
                        lhsResult, rhsResult, result));

                node.setResult(result);
                node.setLvalueResult(null);
                currentFunction.getSymbolTable().put(result.getName(), result);
            } else if (op == BinaryExprNode.Operator.add) {
                // a + b
                if ((node.getLhs().getType() instanceof IntType) && (node.getRhs().getType() instanceof IntType)) {
                    // a + b  for int
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int32), "add");
                    currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.add,
                            lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else {
                    // str1 + str2  for string
                    Function function = module.getExternalFunctionMap().get("__string_concatenate");
                    ArrayList<Operand> parameters = new ArrayList<>();
                    parameters.add(lhsResult);
                    parameters.add(rhsResult);

                    Register result = new Register(new PointerType(new IntegerType(IntegerType.BitWidth.int8)),
                            "stringConcatenate");
                    currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                }
            } else if (op == BinaryExprNode.Operator.less) {
                // a < b
                if ((node.getLhs().getType() instanceof IntType) && (node.getRhs().getType() instanceof IntType)) {
                    // a < b  for int
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "lessThan");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.slt,
                            new IntegerType(IntegerType.BitWidth.int32), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else {
                    // str1 < str2  for string
                    Function function = module.getExternalFunctionMap().get("__string_lessThan");
                    ArrayList<Operand> parameters = new ArrayList<>();
                    parameters.add(lhsResult);
                    parameters.add(rhsResult);

                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1),
                            "stringLessThan");
                    currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                }
            } else if (op == BinaryExprNode.Operator.greater) {
                // a > b
                if ((node.getLhs().getType() instanceof IntType) && (node.getRhs().getType() instanceof IntType)) {
                    // a > b  for int
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "greaterThan");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.sgt,
                            new IntegerType(IntegerType.BitWidth.int32), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else {
                    // str1 > str2  for string
                    Function function = module.getExternalFunctionMap().get("__string_greaterThan");
                    ArrayList<Operand> parameters = new ArrayList<>();
                    parameters.add(lhsResult);
                    parameters.add(rhsResult);

                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1),
                            "stringGreaterThan");
                    currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                }
            } else if (op == BinaryExprNode.Operator.lessEqual) {
                // a <= b
                if ((node.getLhs().getType() instanceof IntType) && (node.getRhs().getType() instanceof IntType)) {
                    // a <= b  for int
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "lessEqual");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.sle,
                            new IntegerType(IntegerType.BitWidth.int32), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else {
                    // str1 < str2  for string
                    Function function = module.getExternalFunctionMap().get("__string_lessEqual");
                    ArrayList<Operand> parameters = new ArrayList<>();
                    parameters.add(lhsResult);
                    parameters.add(rhsResult);

                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1),
                            "stringLessEqual");
                    currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                }
            } else if (op == BinaryExprNode.Operator.greaterEqual) {
                // a >= b
                if ((node.getLhs().getType() instanceof IntType) && (node.getRhs().getType() instanceof IntType)) {
                    // a >= b  for int
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "greaterEqual");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.sge,
                            new IntegerType(IntegerType.BitWidth.int32), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else {
                    // str1 >= str2  for string
                    Function function = module.getExternalFunctionMap().get("__string_greaterEqual");
                    ArrayList<Operand> parameters = new ArrayList<>();
                    parameters.add(lhsResult);
                    parameters.add(rhsResult);

                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1),
                            "stringGreaterEqual");
                    currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                }
            } else if (op == BinaryExprNode.Operator.equal) {
                // a == b
                Type lType = node.getLhs().getType();
                Type rType = node.getRhs().getType();
                if ((lType instanceof IntType) && (rType instanceof IntType)) {
                    // a == b  for int
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "equal");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.eq,
                            new IntegerType(IntegerType.BitWidth.int32), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof BoolType) && (rType instanceof BoolType)) {
                    // a == b  for bool
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "equal");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.eq,
                            new IntegerType(IntegerType.BitWidth.int1), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof StringType) && (rType instanceof StringType)) {
                    // str1 == str2  for string
                    Function function = module.getExternalFunctionMap().get("__string_equal");
                    ArrayList<Operand> parameters = new ArrayList<>();
                    parameters.add(lhsResult);
                    parameters.add(rhsResult);

                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "stringEqual");
                    currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof ArrayType) && (rType instanceof NullType)) {
                    // arr == null  for array
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "equal");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.eq,
                            lhsResult.getType(), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof NullType) && (rType instanceof ArrayType)) {
                    // null == arr  for array
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "equal");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.eq,
                            rhsResult.getType(), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof ClassType) && (rType instanceof NullType)) {
                    // class == null  for class
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "equal");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.eq,
                            lhsResult.getType(), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof NullType) && (rType instanceof ClassType)) {
                    // null == class  for class
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "equal");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.eq,
                            rhsResult.getType(), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof NullType) && (rType instanceof NullType)) {
                    node.setResult(new ConstBool(true));
                    node.setLvalueResult(null);
                }
            } else if (op == BinaryExprNode.Operator.notEqual) {
                // a != b
                Type lType = node.getLhs().getType();
                Type rType = node.getRhs().getType();
                if ((lType instanceof IntType) && (rType instanceof IntType)) {
                    // a != b  for int
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "notEqual");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.ne,
                            new IntegerType(IntegerType.BitWidth.int32), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof BoolType) && (rType instanceof BoolType)) {
                    // a != b  for bool
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "notEqual");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.ne,
                            new IntegerType(IntegerType.BitWidth.int1), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof StringType) && (rType instanceof StringType)) {
                    // str1 != str2  for string
                    Function function = module.getExternalFunctionMap().get("__string_notEqual");
                    ArrayList<Operand> parameters = new ArrayList<>();
                    parameters.add(lhsResult);
                    parameters.add(rhsResult);

                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1),
                            "stringNotEqual");
                    currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof ArrayType) && (rType instanceof NullType)) {
                    // arr != null  for array
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "notEqual");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.ne,
                            lhsResult.getType(), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof NullType) && (rType instanceof ArrayType)) {
                    // null != arr  for array
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "notEqual");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.ne,
                            rhsResult.getType(), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof ClassType) && (rType instanceof NullType)) {
                    // class != null  for class
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "notEqual");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.ne,
                            lhsResult.getType(), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof NullType) && (rType instanceof ClassType)) {
                    // null != class  for class
                    Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "notEqual");
                    currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.ne,
                            rhsResult.getType(), lhsResult, rhsResult, result));

                    node.setResult(result);
                    node.setLvalueResult(null);
                    currentFunction.getSymbolTable().put(result.getName(), result);
                } else if ((lType instanceof NullType) && (rType instanceof NullType)) {
                    node.setResult(new ConstBool(false));
                    node.setLvalueResult(null);
                }
            } else {
                assert op == BinaryExprNode.Operator.assign;
                currentBlock.addInstruction(new StoreInst(currentBlock, rhsResult, node.getLhs().getLvalueResult()));

                node.setResult(rhsResult);
                node.setLvalueResult(null);
            }
        } else if (op == BinaryExprNode.Operator.logicalAnd) {
            // a && b  for bool, short-circuit evaluation
            BasicBlock branchBlock = new BasicBlock(currentFunction, "logicalAndBranch");
            BasicBlock mergeBlock = new BasicBlock(currentFunction, "logicalAndMerge");
            BasicBlock phi1;
            BasicBlock phi2;

            node.getLhs().accept(this); // visit ExprNode
            Operand lhsResult = node.getLhs().getResult();
            currentBlock.addInstruction(new BranchInst(currentBlock, lhsResult, branchBlock, mergeBlock));
            phi1 = currentBlock;

            currentBlock = branchBlock;
            node.getRhs().accept(this); // visit ExprNode
            Operand rhsResult = node.getRhs().getResult();
            currentBlock.addInstruction(new BranchInst(currentBlock, null, mergeBlock, null));
            currentFunction.addBasicBlock(branchBlock);
            phi2 = branchBlock;

            currentBlock = mergeBlock;
            Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "logicalAnd");
            ArrayList<Pair<Operand, BasicBlock>> branch = new ArrayList<>();
            branch.add(new Pair<>(new ConstBool(false), phi1));
            branch.add(new Pair<>(rhsResult, phi2));
            currentBlock.addInstruction(new PhiInst(currentBlock, branch, result));
            currentFunction.addBasicBlock(mergeBlock);

            node.setResult(result);
            node.setLvalueResult(null);
            currentFunction.getSymbolTable().put(result.getName(), result);
            currentFunction.getSymbolTable().put(branchBlock.getName(), branchBlock);
            currentFunction.getSymbolTable().put(mergeBlock.getName(), mergeBlock);
        } else { // op == BinaryExprNode.Operator.logicalOr
            BasicBlock branchBlock = new BasicBlock(currentFunction, "logicalOrBranch");
            BasicBlock mergeBlock = new BasicBlock(currentFunction, "logicalOrMerge");
            BasicBlock phi1;
            BasicBlock phi2;

            node.getLhs().accept(this); // visit ExprNode
            Operand lhsResult = node.getLhs().getResult();
            currentBlock.addInstruction(new BranchInst(currentBlock, lhsResult, mergeBlock, branchBlock));
            phi1 = currentBlock;

            currentBlock = branchBlock;
            node.getRhs().accept(this); // visit ExprNode
            Operand rhsResult = node.getRhs().getResult();
            currentBlock.addInstruction(new BranchInst(currentBlock, null, mergeBlock, null));
            currentFunction.addBasicBlock(branchBlock);
            phi2 = branchBlock;

            currentBlock = mergeBlock;
            Register result = new Register(new IntegerType(IntegerType.BitWidth.int1), "logicalOr");
            ArrayList<Pair<Operand, BasicBlock>> branch = new ArrayList<>();
            branch.add(new Pair<>(new ConstBool(true), phi1));
            branch.add(new Pair<>(rhsResult, phi2));
            currentBlock.addInstruction(new PhiInst(currentBlock, branch, result));
            currentFunction.addBasicBlock(mergeBlock);

            node.setResult(result);
            node.setLvalueResult(null);
            currentFunction.getSymbolTable().put(result.getName(), result);
            currentFunction.getSymbolTable().put(branchBlock.getName(), branchBlock);
            currentFunction.getSymbolTable().put(mergeBlock.getName(), mergeBlock);
        }
    }

    @Override
    public void visit(NewExprNode node) throws CompilationError {
        if (node.getDim() == 0) {
            // class creator
            Type type = astTypeTable.get(node.getBaseType());
            assert type instanceof ClassType;

            Function function = module.getExternalFunctionMap().get("malloc");
            int size = module.getStructureMap().get("class." + type.getName()).getBytes();
            ArrayList<Operand> parameters = new ArrayList<>();
            parameters.add(new ConstInt(size));

            Register result = new Register(new PointerType(new IntegerType(IntegerType.BitWidth.int8)),
                    "malloc");
            Register cast = new Register(type.getIRType(irTypeTable), "classPtr");
            currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, result));
            currentBlock.addInstruction(new BitCastToInst(currentBlock, result, type.getIRType(irTypeTable), cast));

            if (((ClassType) type).hasConstructor()) {
                function = module.getFunctionMap().get(type.getName() + "." + type.getName());
                parameters = new ArrayList<>();
                parameters.add(cast);
                currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, null));
            }

            node.setResult(cast);
            node.setLvalueResult(null);
            currentFunction.getSymbolTable().put(result.getName(), result);
            currentFunction.getSymbolTable().put(cast.getName(), cast);
        } else {
            // array creator
            ArrayList<ExprNode> exprForDim = node.getExprForDim();
            IRType irType = astTypeTable.get(node.getBaseType()).getIRType(irTypeTable);
            for (int i = 0; i < node.getDim(); i++)
                irType = new PointerType(irType);

            ArrayList<Operand> sizeList = new ArrayList<>();
            for (ExprNode expr : exprForDim) {
                expr.accept(this); // visit ExprNode
                sizeList.add(expr.getResult());
            }

            Operand result = NewArrayMalloc.generate(0, sizeList, irType, module, this);
            node.setResult(result);
            node.setLvalueResult(null);
        }
    }

    @Override
    public void visit(MemberExprNode node) throws CompilationError {
        node.getExpr().accept(this); // visit ExprNode

        Type type = node.getExpr().getType();
        assert type instanceof ClassType;

        String name = node.getIdentifier();
        ArrayList<VariableEntity> members = ((ClassType) type).getMembers();
        int pos;
        for (pos = 0; pos < members.size(); pos++)
            if (members.get(pos).getName().equals(name))
                break;

        Operand pointer = node.getExpr().getResult();
        ArrayList<Operand> index = new ArrayList<>();
        index.add(new ConstInt(0));
        index.add(new ConstInt(pos));
        IRType irType = astTypeTable.get(members.get(pos).getType()).getIRType(irTypeTable);
        Register result = new Register(new PointerType(irType), type.getName() + "." + name + "$addr");
        Register load = new Register(irType, type.getName() + "." + name);
        currentBlock.addInstruction(new GetElementPtrInst(currentBlock, pointer, index, result));
        currentBlock.addInstruction(new LoadInst(currentBlock, irType, result, load));

        node.setResult(load);
        node.setLvalueResult(result);
        currentFunction.getSymbolTable().put(result.getName(), result);
        currentFunction.getSymbolTable().put(load.getName(), load);
    }

    @Override
    public void visit(FuncCallExprNode node) throws CompilationError {
        ExprNode funcName = node.getFuncName();
        Function function;
        if (funcName instanceof MemberExprNode) {
            // method call
            ExprNode expr = ((MemberExprNode) funcName).getExpr();
            String name = ((MemberExprNode) funcName).getIdentifier();
            Type type = expr.getType();
            expr.accept(this); // visit ExprNode
            Operand ptrResult = expr.getResult();
            if (type instanceof ArrayType) {
                Register pointer;
                if (!ptrResult.getType().equals(new PointerType(new IntegerType(IntegerType.BitWidth.int32)))) {
                    pointer = new Register(new PointerType(new IntegerType(IntegerType.BitWidth.int32)), "cast");
                    currentBlock.addInstruction(new BitCastToInst(currentBlock, ptrResult,
                            new PointerType(new IntegerType(IntegerType.BitWidth.int32)), pointer));
                    currentFunction.getSymbolTable().put(pointer.getName(), pointer);
                } else
                    pointer = (Register) ptrResult;
                ArrayList<Operand> index = new ArrayList<>();
                index.add(new ConstInt(-1));
                Register result = new Register(pointer.getType(), "elementPtr");
                Register size = new Register(new IntegerType(IntegerType.BitWidth.int32), "arraySize");
                currentBlock.addInstruction(new GetElementPtrInst(currentBlock, pointer, index, result));
                currentBlock.addInstruction(new LoadInst(currentBlock,
                        new IntegerType(IntegerType.BitWidth.int32), result, size));

                node.setResult(size);
                node.setLvalueResult(null);
                currentFunction.getSymbolTable().put(result.getName(), result);
                currentFunction.getSymbolTable().put(size.getName(), size);
            } else {
                if (type instanceof StringType) {
                    function = module.getExternalFunctionMap().get("__string_" + name);
                } else {
                    assert type instanceof ClassType;
                    function = module.getFunctionMap().get(type.getName() + "." + name);
                }
                assert function != null;
                ArrayList<Operand> parameters = new ArrayList<>();
                IRType returnType = function.getFunctionType().getReturnType();
                Register result = returnType instanceof VoidType ? null : new Register(returnType, "call");
                parameters.add(ptrResult);
                for (ExprNode parameterExpr : node.getParameters()) {
                    parameterExpr.accept(this); // visit ExprNode
                    parameters.add(parameterExpr.getResult());
                }

                currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, result));
                if (result != null)
                    currentFunction.getSymbolTable().put(result.getName(), result);

                node.setResult(result);
                node.setLvalueResult(null);
            }
        } else {
            assert funcName instanceof IdExprNode;
            String name = ((IdExprNode) funcName).getIdentifier();
            FunctionEntity functionEntity = node.getScope().getFunctionEntity(name);
            if (functionEntity.getEntityType() == FunctionEntity.EntityType.function) {
                if (module.getFunctionMap().containsKey(name))
                    function = module.getFunctionMap().get(name);
                else
                    function = module.getExternalFunctionMap().get(name);
                assert function != null;
                ArrayList<Operand> parameters = new ArrayList<>();
                IRType returnType = function.getFunctionType().getReturnType();
                Register result = returnType instanceof VoidType ? null : new Register(returnType, "call");
                for (ExprNode parameterExpr : node.getParameters()) {
                    parameterExpr.accept(this); // visit ExprNode
                    parameters.add(parameterExpr.getResult());
                }

                currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, result));
                if (result != null)
                    currentFunction.getSymbolTable().put(result.getName(), result);

                node.setResult(result);
            } else { // Call method: this.method()
                ClassType type = (ClassType) node.getScope().getClassType();
                function = module.getFunctionMap().get(type.getName() + "." + name);
                assert function != null;

                Register thisAllocaAddr = (Register) currentFunction.getSymbolTable().get("this$addr");
                IRType baseType = ((PointerType) thisAllocaAddr.getType()).getBaseType();
                Register ptrResult = new Register(baseType, "this");
                currentBlock.addInstruction(new LoadInst(currentBlock, baseType, thisAllocaAddr, ptrResult));

                ArrayList<Operand> parameters = new ArrayList<>();
                IRType returnType = function.getFunctionType().getReturnType();
                Register result = returnType instanceof VoidType ? null : new Register(returnType, "call");
                parameters.add(ptrResult);
                for (ExprNode parameterExpr : node.getParameters()) {
                    parameterExpr.accept(this); // visit ExprNode
                    parameters.add(parameterExpr.getResult());
                }

                currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, result));
                currentFunction.getSymbolTable().put(ptrResult.getName(), ptrResult);
                if (result != null)
                    currentFunction.getSymbolTable().put(result.getName(), result);

                node.setResult(result);
            }
            node.setLvalueResult(null);
        }
    }

    @Override
    public void visit(SubscriptExprNode node) throws CompilationError {
        node.getName().accept(this); // visit ExprNode
        node.getIndex().accept(this); // visit ExprNode

        Operand arrayPtr = node.getName().getResult();
        ArrayList<Operand> index = new ArrayList<>();
        index.add(node.getIndex().getResult());
        Register result = new Register(arrayPtr.getType(), "elementPtr");
        currentBlock.addInstruction(new GetElementPtrInst(currentBlock, arrayPtr, index, result));

        Register arrayLoad = new Register(((PointerType) arrayPtr.getType()).getBaseType(), "arrayLoad");
        currentBlock.addInstruction(new LoadInst(currentBlock,
                ((PointerType) arrayPtr.getType()).getBaseType(), result, arrayLoad));

        node.setResult(arrayLoad);
        node.setLvalueResult(result);
        currentFunction.getSymbolTable().put(result.getName(), result);
        currentFunction.getSymbolTable().put(arrayLoad.getName(), arrayLoad);
    }

    @Override
    public void visit(ThisExprNode node) throws CompilationError {
        Register thisAllocaAddr = (Register) currentFunction.getSymbolTable().get("this$addr");
        IRType irType = ((PointerType) thisAllocaAddr.getType()).getBaseType();
        Register result = new Register(irType, "this");
        currentBlock.addInstruction(new LoadInst(currentBlock, irType, thisAllocaAddr, result));

        node.setResult(result);
        node.setLvalueResult(null);
        currentFunction.getSymbolTable().put(result.getName(), result);
    }

    @Override
    public void visit(IdExprNode node) throws CompilationError {
        Operand allocaAddr = ((VariableEntity) node.getEntity()).getAllocaAddr();
        if (allocaAddr != null) {
            IRType irType;
            if (((VariableEntity) node.getEntity()).getEntityType() == VariableEntity.EntityType.global)
                irType = allocaAddr.getType();
            else
                irType = ((PointerType) allocaAddr.getType()).getBaseType();
            Register result = new Register(irType, node.getIdentifier());
            currentBlock.addInstruction(new LoadInst(currentBlock, irType, allocaAddr, result));

            node.setResult(result);
            node.setLvalueResult(allocaAddr);
            currentFunction.getSymbolTable().put(result.getName(), result);
        } else {
            Register thisAllocaAddr = (Register) currentFunction.getSymbolTable().get("this$addr");
            assert node.getScope().inMethodScope();
            assert thisAllocaAddr != null;

            IRType baseType = ((PointerType) thisAllocaAddr.getType()).getBaseType();
            Register thisPtr = new Register(baseType, "this");
            currentBlock.addInstruction(new LoadInst(currentBlock, baseType, thisAllocaAddr, thisPtr));

            Type type = node.getScope().getClassType();
            String name = node.getIdentifier();
            ArrayList<VariableEntity> members = ((ClassType) type).getMembers();
            int pos;
            for (pos = 0; pos < members.size(); pos++)
                if (members.get(pos).getName().equals(name))
                    break;

            ArrayList<Operand> index = new ArrayList<>();
            index.add(new ConstInt(0));
            index.add(new ConstInt(pos));
            IRType irType = astTypeTable.get(members.get(pos).getType()).getIRType(irTypeTable);
            Register result = new Register(new PointerType(irType), type.getName() + "." + name + "$addr");
            Register load = new Register(irType, type.getName() + "." + name);
            currentBlock.addInstruction(new GetElementPtrInst(currentBlock, thisPtr, index, result));
            currentBlock.addInstruction(new LoadInst(currentBlock, irType, result, load));

            node.setResult(load);
            node.setLvalueResult(result);
            currentFunction.getSymbolTable().put(thisPtr.getName(), thisPtr);
            currentFunction.getSymbolTable().put(result.getName(), result);
            currentFunction.getSymbolTable().put(load.getName(), load);
        }
    }

    @Override
    public void visit(BoolLiteralNode node) {
        node.setResult(new ConstBool(node.getValue()));
        node.setLvalueResult(null);
    }

    @Override
    public void visit(IntLiteralNode node) {
        node.setResult(new ConstInt(node.getValue()));
        node.setLvalueResult(null);
    }

    @Override
    public void visit(StringLiteralNode node) {
        GlobalVariable string = module.addConstString(node.getValue());
        ArrayList<Operand> index = new ArrayList<>();
        index.add(new ConstInt(0));
        index.add(new ConstInt(0));
        Register result = new Register(new PointerType(new IntegerType(IntegerType.BitWidth.int8)),
                "stringLiteral");
        currentBlock.addInstruction(new GetElementPtrInst(currentBlock, string, index, result));

        node.setResult(result);
        node.setLvalueResult(null);
        currentFunction.getSymbolTable().put(result.getName(), result);
    }

    @Override
    public void visit(NullLiteralNode node) {
        node.setResult(new ConstNull());
        node.setLvalueResult(null);
    }
}
