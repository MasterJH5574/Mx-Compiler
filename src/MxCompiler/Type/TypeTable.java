package MxCompiler.Type;

import MxCompiler.AST.ArrayTypeNode;
import MxCompiler.AST.PrimitiveTypeNode;
import MxCompiler.AST.TypeNode;
import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.ErrorHandler;
import MxCompiler.Utilities.Location;

import java.util.HashMap;
import java.util.Map;

public class TypeTable {
    private Map<TypeNode, Type> typeTable;

    public TypeTable() {
        typeTable = new HashMap<>();

        Location location = new Location(0, 0);
        typeTable.put(new PrimitiveTypeNode(location, "int"), new IntType());
        typeTable.put(new PrimitiveTypeNode(location, "bool"), new BoolType());
        typeTable.put(new PrimitiveTypeNode(location, "string"), new StringType());
        typeTable.put(new PrimitiveTypeNode(location, "void"), new VoidType());
    }

    public boolean hasType(TypeNode typeNode) {
        return typeTable.containsKey(typeNode);
    }

    public void put(TypeNode typeNode, Type type, ErrorHandler errorHandler) throws CompilationError {
        if (hasType(typeNode)) {
            errorHandler.error("Duplicate definition of type \"" + typeNode.toString() + "\".");
            throw new CompilationError();
        } else
            typeTable.put(typeNode, type);
    }

    public Type get(TypeNode typeNode) {
        if (typeNode instanceof ArrayTypeNode) {
            TypeNode baseType = ((ArrayTypeNode) typeNode).getBaseType();
            int dims = ((ArrayTypeNode) typeNode).getDims();
            return new ArrayType(typeTable.get(baseType), dims);
        } else
            return typeTable.get(typeNode);
    }
}
