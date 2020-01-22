package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class VarNode extends ProgramUnitNode {
    private TypeNode type;
    private String identifier;
    private ExprNode initExpr;

    public VarNode(Location location, TypeNode type, String identifier, ExprNode initExpr) {
        super(location);
        this.type = type;
        this.identifier = identifier;
        this.initExpr = initExpr; // If initExpr == null, let it be.
    }

    public TypeNode getType() {
        return type;
    }

    public void setType(TypeNode type) {
        this.type = type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public ExprNode getInitExpr() {
        return initExpr;
    }

    public void setInitExpr(ExprNode initExpr) {
        this.initExpr = initExpr;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
