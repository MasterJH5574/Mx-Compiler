package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;

public interface ASTVisitor {
    // ------ ProgramNode ------
    void visit(ProgramNode node) throws CompilationError;

    // ------ TypeNode -------
    void visit(PrimitiveTypeNode node) throws CompilationError;
    void visit(ClassTypeNode node) throws CompilationError;
    void visit(ArrayTypeNode node) throws CompilationError;

    // ------ ObjectNode ------
    void visit(VarNodeList node) throws CompilationError;
    void visit(VarNode node) throws CompilationError;
    void visit(FunctionNode node) throws CompilationError;
    void visit(ClassNode node) throws CompilationError;

    // ------ StmtNode -------
    void visit(BlockNode node) throws CompilationError;
    void visit(VarDeclStmtNode node) throws CompilationError;
    void visit(IfStmtNode node) throws CompilationError;
    void visit(WhileStmtNode node) throws CompilationError;
    void visit(ForStmtNode node) throws CompilationError;
    void visit(ReturnStmtNode node) throws CompilationError;
    void visit(BreakStmtNode node) throws CompilationError;
    void visit(ContinueStmtNode node) throws CompilationError;
    void visit(ExprStmtNode node) throws CompilationError;

    // ------ ExprNode ------
    void visit(PostfixExprNode node) throws CompilationError;
    void visit(PrefixExprNode node) throws CompilationError;
    void visit(BinaryExprNode node) throws CompilationError;
    void visit(NewExprNode node) throws CompilationError;
    void visit(MemberExprNode node) throws CompilationError;
    void visit(FuncCallExprNode node) throws CompilationError;
    void visit(SubscriptExprNode node) throws CompilationError;
    void visit(ThisExprNode node) throws CompilationError;
    void visit(IdExprNode node) throws CompilationError;

    // ------ ConstExprNode ------
    void visit(BoolLiteralNode node);
    void visit(IntLiteralNode node);
    void visit(StringLiteralNode node);
    void visit(NullLiteralNode node);
}
