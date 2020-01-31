package MxCompiler.AST;

import MxCompiler.Utilities.Location;

public class ClassTypeNode extends TypeNode {
    public ClassTypeNode(Location location, String identifier) {
        super(location, identifier);
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "<ClassTypeNode>\n" + "identifier = " + identifier + "\n";
    }
}
