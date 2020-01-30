package MxCompiler.Frontend;

import MxCompiler.AST.*;
import MxCompiler.Entity.FunctionEntity;
import MxCompiler.Entity.VariableEntity;
import MxCompiler.Type.ClassType;
import MxCompiler.Utilities.ErrorHandler;

import java.util.ArrayList;
import java.util.Stack;

public class Resolver implements ASTVisitor {
    private Scope globalScope;
    private Stack<Scope> scopeStack;
    private TypeTable typeTable;
    private ErrorHandler errorHandler;

    public Resolver(ErrorHandler errorHandler) {
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
                ClassTypeNode classTypeNode = new ClassTypeNode(unit.getLocation(), ((ClassNode) unit).getIdentifier());
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

        if (!typeTable.hasType(node))
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

    }

    @Override
    public void visit(VarNode node) {
        node.setScope(currentScope());

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

    }

    @Override
    public void visit(VarDeclStmtNode node) {

    }

    @Override
    public void visit(IfStmtNode node) {

    }

    @Override
    public void visit(WhileStmtNode node) {

    }

    @Override
    public void visit(ForStmtNode node) {

    }

    @Override
    public void visit(ReturnStmtNode node) {

    }

    @Override
    public void visit(BreakStmtNode node) {

    }

    @Override
    public void visit(ContinueStmtNode node) {

    }

    @Override
    public void visit(ExprStmtNode node) {

    }

    @Override
    public void visit(PostfixExprNode node) {

    }

    @Override
    public void visit(PrefixExprNode node) {

    }

    @Override
    public void visit(BinaryExprNode node) {

    }

    @Override
    public void visit(NewExprNode node) {

    }

    @Override
    public void visit(MemberExprNode node) {

    }

    @Override
    public void visit(FuncCallExprNode node) {

    }

    @Override
    public void visit(SubscriptExprNode node) {

    }

    @Override
    public void visit(ThisExprNode node) {

    }

    @Override
    public void visit(IdExprNode node) {

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
