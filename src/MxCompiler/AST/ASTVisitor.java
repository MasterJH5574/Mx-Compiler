package MxCompiler.AST;

public interface ASTVisitor {
    void visit(ASTNode node);
    void visit(StmtNode node);
    void visit(BlockNode node);
}
