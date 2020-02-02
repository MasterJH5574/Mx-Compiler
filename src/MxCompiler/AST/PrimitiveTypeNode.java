package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.Location;

public class PrimitiveTypeNode extends TypeNode {
    // identifier = int / bool / string / void

    public PrimitiveTypeNode(Location location, String identifier) {
        super(location, identifier);
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "<PrimitiveTypeNode>\nidentifier = " + identifier + "\n";
    }
}
