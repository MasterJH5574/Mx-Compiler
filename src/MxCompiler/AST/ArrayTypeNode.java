package MxCompiler.AST;

import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.Location;

public class ArrayTypeNode extends TypeNode {
    private TypeNode baseType;
    private int dims;

    public ArrayTypeNode(Location location, TypeNode preType) {
        super(location, preType.identifier);
        if (preType instanceof ArrayTypeNode) {
            baseType = ((ArrayTypeNode) preType).baseType;
            dims = ((ArrayTypeNode) preType).dims + 1;
        } else {
            baseType = preType;
            dims = 1;
        }
    }

    public TypeNode getBaseType() {
        return baseType;
    }

    public int getDims() {
        return dims;
    }

    @Override
    public void accept(ASTVisitor visitor) throws CompilationError {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        String head = "<ArrayTypeNode>\n";
        String baseTypeString = "baseType = " + baseType.toString();
        String dimsString = "dims = " + dims + "\n";
        return head + baseTypeString + dimsString;
    }
}
