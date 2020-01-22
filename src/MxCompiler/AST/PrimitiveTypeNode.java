package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class PrimitiveTypeNode extends TypeNode {
    // identifier = int / bool / String / void

    public PrimitiveTypeNode(Location location, String identifier) {
        super(location, identifier);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
