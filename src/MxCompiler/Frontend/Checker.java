package MxCompiler.Frontend;

import MxCompiler.AST.*;
import MxCompiler.Entity.Entity;
import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.Type.ClassType;
import MxCompiler.Utilities.ErrorHandler;

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
    public void visit(ProgramNode node) {
        globalScope = new Scope(null);
        scopeStack.add(globalScope);
        node.setScope(globalScope);

        ArrayList<ProgramUnitNode> programUnitNodes = node.getProgramUnits();
        for (ProgramUnitNode unit : programUnitNodes)   // Step 1: define classes and functions
            if (unit instanceof ClassNode) {
                ClassTypeNode classTypeNode = new ClassTypeNode(unit.getLocation(),
                        ((ClassNode) unit).getIdentifier());
                ClassType classType = ((ClassNode) unit).getClassType();
                typeTable.put(classTypeNode, classType, errorHandler);
            } else if (unit instanceof FunctionNode)
                globalScope.declareEntity(unit, errorHandler,
                        VariableEntity.EntityType.global, FunctionEntity.EntityType.function);

        for (ProgramUnitNode unit : programUnitNodes)   // Step 2: resolve in order.
            if (unit instanceof VarNode) {
                unit.accept(this); // visit VarNode
                globalScope.declareEntity(unit, errorHandler,
                        VariableEntity.EntityType.global, FunctionEntity.EntityType.function);
            } else if (unit instanceof FunctionNode)
                unit.accept(this); // visit FunctionNode
            else if (unit instanceof ClassNode)
                unit.accept(this); // visit ClassNode
            // else do nothing

        scopeStack.pop();
    }

    @Override
    public void visit(PrimitiveTypeNode node) {
        node.setScope(currentScope());

        if (!typeTable.hasType(node)) // The condition can't be true.
            errorHandler.error(node.getLocation(), "Undefined type \"" + node.getIdentifier() + "\".");
    }

    @Override
    public void visit(ClassTypeNode node) {
        node.setScope(currentScope());

        if (!typeTable.hasType(node))
            errorHandler.error(node.getLocation(), "Undefined type \"" + node.getIdentifier() + "\".");
    }

    @Override
    public void visit(ArrayTypeNode node) {
        node.setScope(currentScope());

        if (!typeTable.hasType(node.getBaseType()))
            errorHandler.error(node.getLocation(),
                    "Undefined type \"" + node.getBaseType().getIdentifier() + "\".");
    }

    @Override
    public void visit(VarNodeList node) {
        // This method will never be called.
        node.setScope(currentScope());
    }

    @Override
    public void visit(VarNode node) {
        node.setScope(currentScope());

        if (node.getType().getIdentifier().equals("void")) {
            errorHandler.error(node.getLocation(),
                    "Cannot declare variable \"" + node.getIdentifier() + "\" as void type.");
            return;
        }
        node.getType().accept(this); // visit TypeNode
        if (node.hasInitExpr())
            node.getInitExpr().accept(this); // visit ExprNode
    }

    @Override
    public void visit(FunctionNode node) {
        Scope scope = new Scope(currentScope());
        scopeStack.add(scope);
        node.setScope(scope);

        node.getType().accept(this); // visit TypeNode

        ArrayList<VarNode> parameters = node.getParameters();
        for (VarNode parameter : parameters) {
            parameter.accept(this); // visit VarNode
            scope.declareEntity(parameter, errorHandler,
                    VariableEntity.EntityType.parameter, FunctionEntity.EntityType.function);
        }

        node.getStatement().accept(this); // visit StmtNode

        scopeStack.pop();
    }

    @Override
    public void visit(ClassNode node) {
        Scope scope = new Scope(currentScope());
        scopeStack.add(scope);
        node.setScope(scope);

        ArrayList<VarNode> varList = node.getVarList();
        for (VarNode member : varList) {
            member.accept(this); // visit VarNode
            scope.declareEntity(member, errorHandler,
                    VariableEntity.EntityType.member, FunctionEntity.EntityType.function);
        }

        if (node.hasConstructor())
            node.getConstructor().accept(this); // visit FunctionNode

        ArrayList<FunctionNode> funcList = node.getFuncList();
        for (FunctionNode method : funcList) // Step 1: define methods
            scope.declareEntity(method, errorHandler,
                    VariableEntity.EntityType.global, FunctionEntity.EntityType.method);
        for (FunctionNode method : funcList) // Step 2: resolve functions
            method.accept(this); // visit FunctionNode

        scopeStack.pop();
    }

    @Override
    public void visit(BlockNode node) {
        Scope scope = new Scope(currentScope());
        scopeStack.add(scope);
        node.setScope(scope);

        ArrayList<StmtNode> statements = node.getStatements();
        for (StmtNode statement : statements)
            statement.accept(this); // visit StmtNode

        scopeStack.pop();
    }

    @Override
    public void visit(VarDeclStmtNode node) {
        node.setScope(currentScope());

        ArrayList<VarNode> varList = node.getVarList();
        for (VarNode var : varList) {
            var.accept(this); // visit VarNode
            currentScope().declareEntity(var, errorHandler,
                    VariableEntity.EntityType.local, FunctionEntity.EntityType.function);
        }
    }

    @Override
    public void visit(IfStmtNode node) {
        node.setScope(currentScope());

        node.getCond().accept(this); // visit ExprNode

        if (node.getThenBody() instanceof BlockNode)
            node.getThenBody().accept(this); // visit StmtNode
        else {
            Scope scope = new Scope(currentScope());
            scopeStack.add(scope);

            node.getThenBody().accept(this); // visit StmtNode
            scopeStack.pop();
        }

        if (node.hasElseBody()) {
            if (node.getElseBody() instanceof BlockNode)
                node.getElseBody().accept(this); // visit StmtNode
            else {
                Scope scope = new Scope(currentScope());
                scopeStack.add(scope);

                node.getElseBody().accept(this); // visit StmtNode
                scopeStack.pop();
            }
        }
    }

    @Override
    public void visit(WhileStmtNode node) {
        node.setScope(currentScope());

        node.getCond().accept(this); // visit ExprNode

        if (node.getBody() instanceof BlockNode)
            node.getBody().accept(this); // visit StmtNode
        else {
            Scope scope = new Scope(currentScope());
            scopeStack.add(scope);

            node.getBody().accept(this); // visit StmtNode
            scopeStack.pop();
        }
    }

    @Override
    public void visit(ForStmtNode node) {
        node.setScope(currentScope());

        if (node.hasInit())
            node.getInit().accept(this); // visit ExprNode
        if (node.hasCond())
            node.getCond().accept(this); // visit ExprNode
        if (node.hasStep())
            node.getStep().accept(this); // visit ExprNode

        if (node.getBody() instanceof BlockNode)
            node.getBody().accept(this); // visit StmtNode
        else {
            Scope scope = new Scope(currentScope());
            scopeStack.add(scope);

            node.getBody().accept(this); // visit StmtNode
            scopeStack.pop();
        }
    }

    @Override
    public void visit(ReturnStmtNode node) {
        node.setScope(currentScope());

        if (node.hasReturnValue())
            node.getReturnValue().accept(this); // visit ExprNode
    }

    @Override
    public void visit(BreakStmtNode node) {
        node.setScope(currentScope());
    }

    @Override
    public void visit(ContinueStmtNode node) {
        node.setScope(currentScope());
    }

    @Override
    public void visit(ExprStmtNode node) {
        node.setScope(currentScope());

        node.getExpr().accept(this); // visit ExprNode
    }

    @Override
    public void visit(PostfixExprNode node) {
        node.setScope(currentScope());

        node.getExpr().accept(this); // visit ExprNode
    }

    @Override
    public void visit(PrefixExprNode node) {
        node.setScope(currentScope());

        node.getExpr().accept(this); // visit ExprNode
    }

    @Override
    public void visit(BinaryExprNode node) {
        node.setScope(currentScope());

        node.getLhs().accept(this); // visit ExprNode
        node.getRhs().accept(this); // visit ExprNode
    }

    @Override
    public void visit(NewExprNode node) {
        node.setScope(currentScope());

        if (node.getBaseType().getIdentifier().equals("void")) {
            errorHandler.error(node.getLocation(), "Cannot create array as void type.");
            return;
        }
        node.getBaseType().accept(this); // visit TypeNode

        ArrayList<ExprNode> exprForDim = node.getExprForDim();
        for (ExprNode expr : exprForDim)
            if (expr != null)
                expr.accept(this); // visit ExprNode
    }

    @Override
    public void visit(MemberExprNode node) {
        node.setScope(currentScope());

        node.getExpr().accept(this); // visit ExprNode
    }

    @Override
    public void visit(FuncCallExprNode node) {
        node.setScope(currentScope());

        // funcName: valid: only MemberExpr or idExpr
        //           invalid: others
        ExprNode funcName = node.getFuncName();
        if (funcName instanceof MemberExprNode) {
            // Todo:
            // Do nothing.
            // Whether the MemberExprNode is valid is to be checked later.
        } else if (funcName instanceof IdExprNode) {
            Entity entity = currentScope().getEntity(((IdExprNode) funcName).getIdentifier());
            if (entity == null)
                errorHandler.error(funcName.getLocation(),
                        "Unresolved reference \"" + ((IdExprNode) funcName).getIdentifier() + "\".");
            else if (entity instanceof VariableEntity)
                errorHandler.error(funcName.getLocation(),
                        "\"" + ((IdExprNode) funcName).getIdentifier() + "\" is not a function.");
            else {
                entity.setReferred();
                node.setEntity(entity);
            }
        } else
            errorHandler.error(funcName.getLocation(), "\"" + node.getText() + "\" is not a function.");

        ArrayList<ExprNode> parameters = node.getParameters();
        for (ExprNode parameter : parameters)
            parameter.accept(this); // visit ExprNode
    }

    @Override
    public void visit(SubscriptExprNode node) {
        node.setScope(currentScope());

        node.getName().accept(this);  // visit ExprNode
        node.getIndex().accept(this); // visit ExprNode
    }

    @Override
    public void visit(ThisExprNode node) {
        node.setScope(currentScope());

        // Todo: semantic check
    }

    @Override
    public void visit(IdExprNode node) {
        node.setScope(currentScope());

        Entity entity = currentScope().getEntity(node.getIdentifier());
        if (entity == null)
            errorHandler.error(node.getLocation(),
                    "Unresolved reference \"" + node.getIdentifier() + "\".");
        else if (entity instanceof FunctionEntity)
            errorHandler.error(node.getLocation(),
                    "\"" + node.getIdentifier() + "\" is not a variable reference.");
        else {
            entity.setReferred();
            node.setEntity(entity);
        }
    }

    @Override
    public void visit(BoolLiteralNode node) {
        node.setScope(currentScope());
    }

    @Override
    public void visit(IntLiteralNode node) {
        node.setScope(currentScope());
    }

    @Override
    public void visit(StringLiteralNode node) {
        node.setScope(currentScope());
    }

    @Override
    public void visit(NullLiteralNode node) {
        node.setScope(currentScope());
    }
}
