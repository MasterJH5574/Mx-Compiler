package MxCompiler.AST;

import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class BlockNode extends StmtNode {
    private ArrayList<StmtNode> statements;

    public BlockNode(Location location, ArrayList<StmtNode> statements) {
        super(location);
        this.statements = statements;
    }

    public ArrayList<StmtNode> getStatements() {
        return statements;
    }

    public void addStatement(StmtNode stmt) {
        statements.add(stmt);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
