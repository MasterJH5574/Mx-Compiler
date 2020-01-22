package MxCompiler.AST;

public interface ASTVisitor {
    // ------ ProgramNode ------
    void visit(ProgramNode node);

    // ------ TypeNode -------
    void visit(PrimitiveTypeNode node);
    void visit(ClassTypeNode node);
    void visit(ArrayTypeNode node);

    // ------ ObjectNode ------
    void visit(VarNode node);
    void visit(FunctionNode node);
    void visit(ClassNode node);

    // ------ StmtNode -------
    void visit(BlockNode node);
    void visit(VarDeclStmtNode node);
    void visit(IfStmtNode node);
    void visit(WhileStmtNode node);
    void visit(ForStmtNode node);
    void visit(ReturnStmtNode node);
    void visit(BreakStmtNode node);
    void visit(ContinueStmtNode node);
    void visit(ExprStmtNode node);

    // ------ ExprNode ------
    void visit(PostfixExprNode node);
    void visit(PrefixExprNode node);
    void visit(BinaryExprNode node);
    void visit(NewExprNode node);
    void visit(MemberExprNode node);
    void visit(FuncCallExprNode node);
    void visit(SubscriptExprNode node);
    void visit(ThisExprNode node);
    void visit(IdExprNode node);

    // ------ ConstExprNode ------
    void visit(BoolLiteralNode node);
    void visit(IntLiteralNode node);
    void visit(StringLiteralNode node);
    void visit(NullLiteralNode node);
}
