package MxCompiler.Frontend;

import MxCompiler.AST.*;
import MxCompiler.Entity.Entity;
import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.Type.*;
import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.ErrorHandler;
import MxCompiler.Utilities.Location;

import java.util.ArrayList;
import java.util.Stack;

public class Checker implements ASTVisitor {
    private Scope globalScope;
    private Stack<Scope> scopeStack;
    private TypeTable typeTable;
    private ErrorHandler errorHandler;

    public Checker(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        scopeStack = new Stack<>();
        typeTable = new TypeTable();
    }

    public Scope getGlobalScope() {
        return globalScope;
    }

    private Scope currentScope() {
        return scopeStack.peek();
    }

    @Override
    public void visit(ProgramNode node) throws CompilationError {
        globalScope = new Scope(null, Scope.ScopeType.programScope,
                null, null);
        scopeStack.add(globalScope);
        node.setScope(globalScope);

        globalScope.addBuiltInFunction();

        boolean error = false;
        ArrayList<ProgramUnitNode> programUnitNodes = node.getProgramUnits();
        for (ProgramUnitNode unit : programUnitNodes)   // Step 1: define classes
            if (unit instanceof ClassNode) {
                ClassTypeNode classTypeNode = new ClassTypeNode(unit.getLocation(),
                        ((ClassNode) unit).getIdentifier());
                ClassType classType = ((ClassNode) unit).getClassType();
                try {
                    typeTable.put(classTypeNode, classType, errorHandler);
                } catch (CompilationError ignored) {
                    error = true;
                }
            }

        for (ProgramUnitNode unit : programUnitNodes)   // Step 2: define functions
            if (unit instanceof FunctionNode)
                try {
                    globalScope.declareEntity(unit, errorHandler, VariableEntity.EntityType.global,
                            FunctionEntity.EntityType.function, globalScope, typeTable);
                } catch (CompilationError ignored) {
                    error = true;
                }

        for (ProgramUnitNode unit : programUnitNodes)   // Step 3: resolve in order.
            try {
                if (unit instanceof VarNode) {
                    unit.accept(this); // visit VarNode
                    globalScope.declareEntity(unit, errorHandler, VariableEntity.EntityType.global,
                            FunctionEntity.EntityType.function, globalScope, typeTable);
                } else if (unit instanceof FunctionNode)
                    unit.accept(this); // visit FunctionNode
                else if (unit instanceof ClassNode)
                    unit.accept(this); // visit ClassNode
                // else do nothing
            } catch (CompilationError ignored) {
                error = true;
            }

        // Step 4: check "int main()"
        Entity mainFunction = currentScope().getEntity("main");
        if (!(mainFunction instanceof FunctionEntity)) {
            errorHandler.error("Main function not found.");
            error = true;
        } else {
            if (!typeTable.get(((FunctionEntity) mainFunction).getReturnType()).equals(new IntType())) {
                errorHandler.error(mainFunction.getLocation(),
                        "Return value type of function \"main()\" is not int.");
                error = true;
            }
            if (((FunctionEntity) mainFunction).getParameters().size() != 0) {
                errorHandler.error(mainFunction.getLocation(),
                        "Main function should have no parameter.");
                error = true;
            }
        }

        scopeStack.pop();

        if (error)
            throw new CompilationError();
    }

    @Override
    public void visit(PrimitiveTypeNode node) throws CompilationError {
        node.setScope(currentScope());

        if (!typeTable.hasType(node)) { // The condition can't be true.
            errorHandler.error(node.getLocation(), "Undefined type \"" + node.getIdentifier() + "\".");
            throw new CompilationError();
        }
    }

    @Override
    public void visit(ClassTypeNode node) throws CompilationError {
        node.setScope(currentScope());

        if (!typeTable.hasType(node)) {
            errorHandler.error(node.getLocation(), "Undefined type \"" + node.getIdentifier() + "\".");
            throw new CompilationError();
        }
    }

    @Override
    public void visit(ArrayTypeNode node) throws CompilationError {
        node.setScope(currentScope());

        if (!typeTable.hasType(node.getBaseType())) {
            errorHandler.error(node.getLocation(),
                    "Undefined type \"" + node.getBaseType().getIdentifier() + "\".");
            throw new CompilationError();
        }
    }

    @Override
    public void visit(VarNodeList node) throws CompilationError {
        // This method will never be called.
        node.setScope(currentScope());
    }

    @Override
    public void visit(VarNode node) throws CompilationError {
        node.setScope(currentScope());

        if (node.getType().getIdentifier().equals("void")) {
            errorHandler.error(node.getLocation(),
                    "Cannot declare variable \"" + node.getIdentifier() + "\" as void type.");
            throw new CompilationError();
        }
        node.getType().accept(this); // visit TypeNode
        if (node.hasInitExpr()) {
            node.getInitExpr().accept(this); // visit ExprNode
            Type lType = typeTable.get(node.getType());
            Type rType = node.getInitExpr().getType();
            if (Type.canNotAssign(lType, rType)) {
                errorHandler.error(node.getLocation(), "Type of rhs \"" +
                        rType.toString() + "\" is not \"" + lType.toString() + "\".");
                throw new CompilationError();
            }
        }
    }

    @Override
    public void visit(FunctionNode node) throws CompilationError {
        Scope scope = new Scope(currentScope(), Scope.ScopeType.functionScope,
                node.getType(), currentScope().getClassType());
        scopeStack.add(scope);
        node.setScope(scope);

        try {
            node.getType().accept(this); // visit TypeNode
        } catch (CompilationError ignored) {
            scopeStack.pop();
            throw new CompilationError();
        }

        boolean error = false;
        ArrayList<VarNode> parameters = node.getParameters();
        for (VarNode parameter : parameters)
            try {
                parameter.accept(this); // visit VarNode
                scope.declareEntity(parameter, errorHandler, VariableEntity.EntityType.parameter,
                        FunctionEntity.EntityType.function, globalScope, typeTable);
            } catch (CompilationError ignored) {
                error = true;
            }

        try {
            node.getStatement().accept(this); // visit StmtNode
        } catch (CompilationError ignored) {
            error = true;
        }

        // check whether there is at least a return statement
        // Question: Maybe in IR stage?
        if (!typeTable.get(node.getType()).equals(new VoidType()) && !node.getIdentifier().equals("main")) {
            assert node.getStatement() instanceof BlockNode;
            BlockNode block = (BlockNode) node.getStatement();
            ArrayList<StmtNode> statements = block.getStatements();
            boolean returnOccur = false;
            for (StmtNode statement : statements)
                if (statement instanceof ReturnStmtNode) {
                    returnOccur = true;
                    break;
                }
            if (!returnOccur) {
                errorHandler.error(node.getLocation(), "Function has no return statement.");
                error = true;
            }
        }

        scopeStack.pop();

        if (error)
            throw new CompilationError();
    }

    @Override
    public void visit(ClassNode node) throws CompilationError {
        Scope scope = new Scope(currentScope(), Scope.ScopeType.classScope, null,
                typeTable.get(new ClassTypeNode(new Location(0, 0), node.getIdentifier())));
        scopeStack.add(scope);
        node.setScope(scope);

        boolean error = false;
        ArrayList<VarNode> varList = node.getVarList();
        for (VarNode member : varList)
            try {
                member.accept(this); // visit VarNode
                scope.declareEntity(member, errorHandler, VariableEntity.EntityType.member,
                        FunctionEntity.EntityType.function, globalScope, typeTable);
            } catch (CompilationError ignored) {
                error = true;
            }

        ArrayList<FunctionNode> funcList = node.getFuncList();
        for (FunctionNode method : funcList) { // Step 1: define methods
            if (method.getIdentifier().equals(node.getIdentifier())) {
                errorHandler.error(method.getLocation(),
                        "Return type specification for constructor is invalid.");
                error = true;
                continue;
            }
            try {
                scope.declareEntity(method, errorHandler, VariableEntity.EntityType.global,
                        FunctionEntity.EntityType.method, globalScope, typeTable);
            } catch (CompilationError ignored) {
                error = true;
            }
        }

        if (node.hasConstructor()) // Step 2: resolve constructor
            try {
                node.getConstructor().accept(this); // visit FunctionNode
            } catch (CompilationError ignored) {
                error = true;
            }

        for (FunctionNode method : funcList) { // Step 3: resolve functions
            if (!method.getIdentifier().equals(node.getIdentifier()))
                try {
                    method.accept(this); // visit FunctionNode
                } catch (CompilationError ignored) {
                    error = true;
                }
        }

        scopeStack.pop();

        if (error)
            throw new CompilationError();
    }

    @Override
    public void visit(BlockNode node) throws CompilationError {
        Scope scope = new Scope(currentScope(), Scope.ScopeType.blockScope,
                currentScope().getFunctionReturnType(), currentScope().getClassType());
        scopeStack.add(scope);
        node.setScope(scope);

        boolean error = false;
        ArrayList<StmtNode> statements = node.getStatements();
        for (StmtNode statement : statements)
            try {
                statement.accept(this); // visit StmtNode
            } catch (CompilationError ignored) {
                error = true;
            }

        scopeStack.pop();

        if (error)
            throw new CompilationError();
    }

    @Override
    public void visit(VarDeclStmtNode node) throws CompilationError {
        node.setScope(currentScope());

        boolean error = false;
        ArrayList<VarNode> varList = node.getVarList();
        for (VarNode var : varList) {
            var.accept(this); // visit VarNode
            try {
                currentScope().declareEntity(var, errorHandler, VariableEntity.EntityType.local,
                        FunctionEntity.EntityType.function, globalScope, typeTable);
            } catch (CompilationError ignored) {
                error = true;
            }
        }

        if (error)
            throw new CompilationError();
    }

    @Override
    public void visit(IfStmtNode node) throws CompilationError {
        node.setScope(currentScope());

        boolean error = false;

        try {
            node.getCond().accept(this); // visit ExprNode
            if (!node.getCond().getType().equals(new BoolType())) {
                errorHandler.error(node.getCond().getLocation(), "The condition should be bool type.");
                throw new CompilationError();
            }
        } catch (CompilationError ignored) {
            error = true;
        }

        if (node.hasThenBody()) {
            try {
                if (node.getThenBody() instanceof BlockNode)
                    node.getThenBody().accept(this); // visit StmtNode
                else {
                    Scope scope = new Scope(currentScope(), Scope.ScopeType.blockScope,
                            currentScope().getFunctionReturnType(), currentScope().getClassType());
                    scopeStack.add(scope);

                    node.getThenBody().accept(this); // visit StmtNode
                    scopeStack.pop();
                }
            } catch (CompilationError ignored) {
                error = true;
            }
        }

        if (node.hasElseBody()) {
            try {
                if (node.getElseBody() instanceof BlockNode)
                    node.getElseBody().accept(this); // visit StmtNode
                else {
                    Scope scope = new Scope(currentScope(), Scope.ScopeType.blockScope,
                            currentScope().getFunctionReturnType(), currentScope().getClassType());
                    scopeStack.add(scope);

                    node.getElseBody().accept(this); // visit StmtNode
                    scopeStack.pop();
                }
            } catch (CompilationError ignored) {
                error = true;
            }
        }

        if (error)
            throw new CompilationError();
    }

    @Override
    public void visit(WhileStmtNode node) throws CompilationError{
        node.setScope(currentScope());

        boolean error = false;

        try {
            node.getCond().accept(this); // visit ExprNode
            if (!node.getCond().getType().equals(new BoolType())) {
                errorHandler.error(node.getCond().getLocation(), "The condition should be bool type.");
                throw new CompilationError();
            }
        } catch (CompilationError ignored) {
            error = true;
        }

        if (node.hasBody()) {
            Scope scope = new Scope(currentScope(), Scope.ScopeType.loopScope,
                    currentScope().getFunctionReturnType(), currentScope().getClassType());
            scopeStack.add(scope);

            try {
                node.getBody().accept(this); // visit StmtNode
            } catch (CompilationError ignored) {
                error = true;
            }
            scopeStack.pop();
        }

        if (error)
            throw new CompilationError();
    }

    @Override
    public void visit(ForStmtNode node) throws CompilationError {
        node.setScope(currentScope());
        boolean error = false;

        if (node.hasInit())
            try {
                node.getInit().accept(this); // visit ExprNode
            } catch (CompilationError ignored) {
                error = true;
            }
        if (node.hasCond()) {
            try {
                node.getCond().accept(this); // visit ExprNode
                if (!node.getCond().getType().equals(new BoolType())) {
                    errorHandler.error(node.getCond().getLocation(), "The condition should be bool type.");
                    throw new CompilationError();
                }
            } catch (CompilationError ignored) {
                error = true;
            }
        }
        if (node.hasStep())
            try {
                node.getStep().accept(this); // visit ExprNode
            } catch (CompilationError ignored) {
                error = true;
            }

        if (node.hasBody()) {
            Scope scope = new Scope(currentScope(), Scope.ScopeType.loopScope,
                    currentScope().getFunctionReturnType(), currentScope().getClassType());
            scopeStack.add(scope);

            try {
                node.getBody().accept(this); // visit StmtNode
            } catch (CompilationError ignored) {
                error = true;
            }
            scopeStack.pop();
        }

        if (error)
            throw new CompilationError();
    }

    @Override
    public void visit(ReturnStmtNode node) throws CompilationError {
        node.setScope(currentScope());

        if (!currentScope().inFunctionScope()) {
            errorHandler.error(node.getLocation(), "The return statement is not in a function scope.");
            throw new CompilationError();
        }
        Type lType = typeTable.get(currentScope().getFunctionReturnType());
        if (node.hasReturnValue()) {
            node.getReturnValue().accept(this); // visit ExprNode
            ExprNode returnValue = node.getReturnValue();
            if (lType.equals(new VoidType())) {
                errorHandler.error(returnValue.getLocation(), "The function requires void return type.");
                throw new CompilationError();
            }
            Type rType = returnValue.getType();
            if (Type.canNotAssign(lType, rType)) {
                errorHandler.error(returnValue.getLocation(),
                        "\"" + returnValue.getText() + "\" is not " + lType.toString() + " type.");
                throw new CompilationError();
            }
        } else {
            if (!lType.equals(new VoidType())) {
                errorHandler.error(node.getLocation(), "The function should have no return value.");
                throw new CompilationError();
            }
        }
    }

    @Override
    public void visit(BreakStmtNode node) throws CompilationError {
        node.setScope(currentScope());

        if (!currentScope().inLoopScope()) {
            errorHandler.error(node.getLocation(), "The break statement is not in a loop scope.");
            throw new CompilationError();
        }
    }

    @Override
    public void visit(ContinueStmtNode node) throws CompilationError {
        node.setScope(currentScope());

        if (!currentScope().inLoopScope()) {
            errorHandler.error(node.getLocation(), "The continue statement is not in a loop scope.");
            throw new CompilationError();
        }
    }

    @Override
    public void visit(ExprStmtNode node) throws CompilationError {
        node.setScope(currentScope());

        node.getExpr().accept(this); // visit ExprNode
    }

    @Override
    public void visit(PostfixExprNode node) throws CompilationError {
        node.setScope(currentScope());

        node.getExpr().accept(this); // visit ExprNode

        // a++
        Location location = node.getExpr().getLocation();
        String text = node.getExpr().getText();
        if (!(node.getExpr().getType() instanceof IntType)) {
            errorHandler.error(location, "\"" + text + "\" is not int type.");
            throw new CompilationError();
        }
        if (!node.getExpr().getLvalue()) {
            errorHandler.error(location, "\"" + text + "\" is not lvalue.");
            throw new CompilationError();
        }
        node.setLvalue(false);
        node.setType(new IntType());
    }

    @Override
    public void visit(PrefixExprNode node) throws CompilationError {
        node.setScope(currentScope());

        node.getExpr().accept(this); // visit ExprNode

        PrefixExprNode.Operator op = node.getOp();

        ExprNode expr = node.getExpr();
        Location location = expr.getLocation();
        String text = expr.getText();
        Type type = expr.getType();

        if (op == PrefixExprNode.Operator.preInc || op == PrefixExprNode.Operator.preDec) {
            // ++a
            if (!(type instanceof IntType)) {
                errorHandler.error(location, "\"" + text + "\" is not int type.");
                throw new CompilationError();
            }
            if (!expr.getLvalue()) {
                errorHandler.error(location, "\"" + text + "\" is not lvalue.");
                throw new CompilationError();
            }
            node.setLvalue(true);
            node.setType(new IntType());
        } else if (op == PrefixExprNode.Operator.signPos || op == PrefixExprNode.Operator.signNeg) {
            // +a, -a
            if (!(type instanceof IntType)) {
                errorHandler.error(location, "\"" + text + "\" is not int type.");
                throw new CompilationError();
            }
            node.setLvalue(false);
            node.setType(new IntType());
        } else if (op == PrefixExprNode.Operator.logicalNot) {
            // !a
            if (!(type instanceof BoolType)) {
                errorHandler.error(location, "\"" + text + "\" is not bool type.");
                throw new CompilationError();
            }
            node.setLvalue(false);
            node.setType(new BoolType());
        } else {
            // ~a
            assert op == PrefixExprNode.Operator.bitwiseComplement;
            if (!(type instanceof IntType)) {
                errorHandler.error(location, "\"" + text + "\" is not int type.");
                throw new CompilationError();
            }
            node.setLvalue(false);
            node.setType(new IntType());
        }
    }

    @Override
    public void visit(BinaryExprNode node) throws CompilationError {
        node.setScope(currentScope());

        node.getLhs().accept(this); // visit ExprNode
        node.getRhs().accept(this); // visit ExprNode

        BinaryExprNode.Operator op = node.getOp();

        ExprNode lExpr = node.getLhs();
        ExprNode rExpr = node.getRhs();
        Location lLocation = lExpr.getLocation();
        Location rLocation = rExpr.getLocation();
        Type lType = lExpr.getType();
        Type rType = rExpr.getType();
        String lText = lExpr.getText();
        String rText = rExpr.getText();

        if (op == BinaryExprNode.Operator.mul ||
                op == BinaryExprNode.Operator.div ||
                op == BinaryExprNode.Operator.mod ||
                op == BinaryExprNode.Operator.sub ||
                op == BinaryExprNode.Operator.shiftLeft ||
                op == BinaryExprNode.Operator.shiftRight ||
                op == BinaryExprNode.Operator.bitwiseAnd ||
                op == BinaryExprNode.Operator.bitwiseXor ||
                op == BinaryExprNode.Operator.bitwiseOr) {
            // *  /  %  -  <<  >>  &  ^  |  for int
            if (!(lType instanceof IntType)) {
                errorHandler.error(lLocation, "\"" + lText + "\" is not int type.");
                throw new CompilationError();
            }
            if (!(rType instanceof IntType)) {
                errorHandler.error(rLocation, "\"" + rText + "\" is not int type.");
                throw new CompilationError();
            }
            node.setLvalue(false);
            node.setType(new IntType());
        } else if (op == BinaryExprNode.Operator.add) {
            // +  for int or string
            if ((lType instanceof IntType) && (rType instanceof IntType)) {
                node.setLvalue(false);
                node.setType(new IntType());
            } else if ((lType instanceof StringType) && (rType instanceof StringType)) {
                node.setLvalue(false);
                node.setType(new StringType());
            } else {
                errorHandler.error(node.getLocation(), "Invalid expression \"" + node.getText() + "\".");
                throw new CompilationError();
            }
        } else if (op == BinaryExprNode.Operator.less ||
                op == BinaryExprNode.Operator.greater ||
                op == BinaryExprNode.Operator.lessEqual ||
                op == BinaryExprNode.Operator.greaterEqual) {
            // <  >  <=  >=  for int or string
            if (((lType instanceof IntType) && (rType instanceof IntType)) ||             // int
                    ((lType instanceof StringType) && (rType instanceof StringType))) {   // string
                node.setLvalue(false);
                node.setType(new BoolType());
            } else {
                errorHandler.error(node.getLocation(), "Invalid expression \"" + node.getText() + "\".");
                throw new CompilationError();
            }
        } else if (op == BinaryExprNode.Operator.equal
                || op == BinaryExprNode.Operator.notEqual) {
            // ==  !=  for int, bool, string, ArrayType and ClassType
            if (((lType instanceof IntType) && (rType instanceof IntType)) ||
                    ((lType instanceof BoolType) && (rType instanceof BoolType)) ||
                    ((lType instanceof StringType) && (rType instanceof StringType)) ||
                    ((lType instanceof ArrayType) && (rType instanceof NullType)) ||
                    ((lType instanceof NullType) && (rType instanceof ArrayType)) ||
                    ((lType instanceof ClassType) && (rType instanceof NullType)) ||
                    ((lType instanceof NullType) && (rType instanceof ClassType)) ||
                    ((lType instanceof NullType) && (rType instanceof NullType))) {
                node.setLvalue(false);
                node.setType(new BoolType());
            } else {
                errorHandler.error(node.getLocation(), "Invalid expression \"" + node.getText() + "\".");
                throw new CompilationError();
            }
        } else if (op == BinaryExprNode.Operator.logicalAnd ||
                op == BinaryExprNode.Operator.logicalOr) {
            // &&  ||  for bool
            if (!(lType instanceof BoolType)) {
                errorHandler.error(lLocation, "\"" + lText + "\" is not bool type.");
                throw new CompilationError();
            }
            if (!(rType instanceof BoolType)) {
                errorHandler.error(rLocation, "\"" + rText + "\" is not bool type.");
                throw new CompilationError();
            }
            node.setLvalue(false);
            node.setType(new BoolType());
        } else {
            assert op == BinaryExprNode.Operator.assign;
            if (!lExpr.getLvalue()) {
                errorHandler.error(lLocation, "\"" + lText + "\" is not lvalue.");
                throw new CompilationError();
            }
            if (Type.canNotAssign(lType, rType)) {
                errorHandler.error(node.getLocation(), "Type of rhs \"" +
                        rType.toString() + "\" is not \"" + lType.toString() + "\".");
                throw new CompilationError();
            }
            node.setLvalue(false);
            node.setType(lType);
        }
    }

    @Override
    public void visit(NewExprNode node) throws CompilationError {
        node.setScope(currentScope());

        if (node.getDim() == -1) {
            // wrong creator
            assert node.getExprForDim() == null;
            // The error has already been added to errorHandler.
            throw new CompilationError();
        }

        // However, according to Parser, it seems that the following condition cannot be true.
        if (node.getBaseType().getIdentifier().equals("void")) {
            errorHandler.error(node.getLocation(), "Cannot create array as void type.");
            throw new CompilationError();
        }
        node.getBaseType().accept(this); // visit TypeNode

        if (node.getDim() == 0) {
            // class creator
            Type type = typeTable.get(node.getBaseType());
            // Expression such as "new int" is invalid.
            if (type.equals(new IntType()) || type.equals(new BoolType()) || type.equals(new StringType())) {
                errorHandler.error(node.getBaseType().getLocation(),
                        "Cannot create an instance of type " + type.toString() + ".");
                throw new CompilationError();
            }
            node.setLvalue(true);
            node.setType(type);
        } else {
            // array creator
            ArrayList<ExprNode> exprForDim = node.getExprForDim();
            for (ExprNode expr : exprForDim) {
                assert expr != null;
                expr.accept(this); // visit ExprNode
                if (!(expr.getType() instanceof IntType)) {
                    errorHandler.error(expr.getLocation(),
                            "Expression \"" + expr.getText() + "\" is not int type.");
                    throw new CompilationError();
                }
            }

            Type baseType = typeTable.get(node.getBaseType());
            node.setLvalue(true); // Question: to be check
            node.setType(new ArrayType(baseType, node.getDim()));
        }
    }

    @Override
    public void visit(MemberExprNode node) throws CompilationError {
        node.setScope(currentScope());

        node.getExpr().accept(this); // visit ExprNode

        ExprNode expr = node.getExpr();
        Type type = expr.getType();
        String name = node.getIdentifier();

        String errorMessage = "\"" + expr.getText() + "\" has no member or method named \"" + name + "\".";
        if (type instanceof ArrayType) {
            if (!((ArrayType) type).hasMethod(name)) {
                errorHandler.error(expr.getLocation(), errorMessage);
                throw new CompilationError();
            }
            node.setLvalue(false);
            node.setType(new MethodType(name, type));
        } else if (type instanceof StringType) {
            if (!((StringType) type).hasMethod(name)) {
                errorHandler.error(expr.getLocation(), errorMessage);
                throw new CompilationError();
            }
            node.setLvalue(false);
            node.setType(new MethodType(name, type));
        } else if (type instanceof ClassType) {
            if (!((ClassType) type).hasMemberOrMethod(name)) {
                errorHandler.error(expr.getLocation(), errorMessage);
                throw new CompilationError();
            }
            if (((ClassType) type).hasMember(name)) {
                VariableEntity member = ((ClassType) type).getMember(name);
                TypeNode memberType = member.getType();
                node.setLvalue(true);
                node.setType(typeTable.get(memberType));
            } else {
                assert ((ClassType) type).hasMethod(name);
                node.setLvalue(false);
                node.setType(new MethodType(name, type));
            }
        } else {
            errorHandler.error(expr.getLocation(), errorMessage);
            throw new CompilationError();
        }
    }

    @Override
    public void visit(FuncCallExprNode node) throws CompilationError {
        node.setScope(currentScope());

        // funcName: valid: only MemberExpr or idExpr
        //           invalid: others
        ExprNode funcName = node.getFuncName();
        if (funcName instanceof MemberExprNode) {
            funcName.accept(this); // visit ExprNode
            if (!(funcName.getType() instanceof MethodType)) {
                errorHandler.error(funcName.getLocation(), "\"" + funcName.getText() + "\" is not a method.");
                throw new CompilationError();
            } // else good
        } else if (funcName instanceof IdExprNode) {
            Entity entity = currentScope().getEntity(((IdExprNode) funcName).getIdentifier());
            if (entity == null) {
                errorHandler.error(funcName.getLocation(),
                        "Unresolved reference \"" + ((IdExprNode) funcName).getIdentifier() + "\".");
                throw new CompilationError();
            } else if (entity instanceof VariableEntity) {
                errorHandler.error(funcName.getLocation(),
                        "\"" + ((IdExprNode) funcName).getIdentifier() + "\" is not a function.");
                throw new CompilationError();
            } // else good
        } else {
            errorHandler.error(funcName.getLocation(), "\"" + node.getText() + "\" is not a function.");
            throw new CompilationError();
        }

        ArrayList<ExprNode> parameters = node.getParameters();
        for (ExprNode parameter : parameters)
            parameter.accept(this); // visit ExprNode

        FunctionEntity function;
        ArrayList<VariableEntity> funcParameters;
        if (funcName instanceof MemberExprNode) {
            assert funcName.getType() instanceof MethodType;
            MethodType methodType = (MethodType) funcName.getType();
            if (methodType.getType() instanceof ArrayType) {
                function = ((ArrayType) methodType.getType()).getMethod(methodType.getName());
            } else if (methodType.getType() instanceof StringType) {
                function = ((StringType) methodType.getType()).getMethod(methodType.getName());
            } else {
                assert methodType.getType() instanceof ClassType;
                function = ((ClassType) methodType.getType()).getMethod(methodType.getName());
            }
        } else // funcName instanceof IdExprNode
            function = (FunctionEntity) currentScope().getEntity(((IdExprNode) funcName).getIdentifier());

        // Check parameters.
        funcParameters = (function).getParameters();
        if (parameters.size() != funcParameters.size()) {
            errorHandler.error(node.getLocation(), "Number of parameters is not consistent.");
            throw new CompilationError();
        }
        for (int i = 0; i < parameters.size(); i++) {
            ExprNode rhs = parameters.get(i);
            VariableEntity lhs = funcParameters.get(i);
            Type rType = rhs.getType();
            Type lType = typeTable.get(lhs.getType());
            if (Type.canNotAssign(lType, rType)) {
                errorHandler.error(rhs.getLocation(), "Type of \"" + rhs.getText()
                        + "\" is not \"" + lType.toString() + "\".");
                throw new CompilationError();
            }
        }

        node.setLvalue(false);
        node.setEntity(function);
        node.setType(typeTable.get(function.getReturnType()));
    }

    @Override
    public void visit(SubscriptExprNode node) throws CompilationError {
        node.setScope(currentScope());

        node.getName().accept(this);  // visit ExprNode
        node.getIndex().accept(this); // visit ExprNode

        ExprNode name = node.getName();
        ExprNode index = node.getIndex();

        Type nameType = name.getType();
        if (!(nameType instanceof ArrayType)) {
            errorHandler.error(name.getLocation(), "\"" + name.getText() + "\" is not array type.");
            throw new CompilationError();
        }
        Type indexType = index.getType();
        if (!(indexType instanceof IntType)) {
            errorHandler.error(index.getLocation(), "\"" + index.getText() + "\" is not int type");
            throw new CompilationError();
        }

        node.setLvalue(name.getLvalue());
        Type baseType = ((ArrayType) nameType).getBaseType();
        int dims = ((ArrayType) nameType).getDims();
        if (dims == 1)
            node.setType(baseType);
        else
            node.setType(new ArrayType(baseType, dims - 1));
    }

    @Override
    public void visit(ThisExprNode node) throws CompilationError {
        node.setScope(currentScope());

        if (!currentScope().inMethodScope()) {
            errorHandler.error(node.getLocation(), "The \"this\" is not in a method.");
            throw new CompilationError();
        }
        node.setLvalue(true);
        node.setType(currentScope().getClassType());
    }

    @Override
    public void visit(IdExprNode node) throws CompilationError {
        node.setScope(currentScope());

        Entity entity = currentScope().getEntity(node.getIdentifier());
        if (entity == null) {
            errorHandler.error(node.getLocation(),
                    "Unresolved reference \"" + node.getIdentifier() + "\".");
            throw new CompilationError();
        } else if (entity instanceof FunctionEntity) {
            errorHandler.error(node.getLocation(),
                    "\"" + node.getIdentifier() + "\" is not a variable reference.");
            throw new CompilationError();
        }

        assert entity instanceof VariableEntity;
        entity.setReferred();
        node.setEntity(entity);
        node.setLvalue(true);
        node.setType(typeTable.get(((VariableEntity) entity).getType()));
    }

    @Override
    public void visit(BoolLiteralNode node) {
        node.setScope(currentScope());

        node.setLvalue(false);
        node.setType(new BoolType());
    }

    @Override
    public void visit(IntLiteralNode node) {
        node.setScope(currentScope());

        node.setLvalue(false);
        node.setType(new IntType());
    }

    @Override
    public void visit(StringLiteralNode node) {
        node.setScope(currentScope());

        node.setLvalue(false);
        node.setType(new StringType());
    }

    @Override
    public void visit(NullLiteralNode node) {
        node.setScope(currentScope());

        node.setLvalue(false);
        node.setType(new NullType());
    }
}
