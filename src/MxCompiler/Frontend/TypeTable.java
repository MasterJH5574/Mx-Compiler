package MxCompiler.Frontend;

import MxCompiler.AST.PrimitiveTypeNode;
import MxCompiler.AST.TypeNode;
import MxCompiler.Type.*;
import MxCompiler.Utilities.ErrorHandler;
import MxCompiler.Utilities.Location;

import java.util.HashMap;
import java.util.Map;

public class TypeTable {
    private Map<TypeNode, Type> typeTable; // Question: Location influences the hash code?

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

    public void put(TypeNode typeNode, Type type, ErrorHandler errorHandler) {
        if (hasType(typeNode))
            errorHandler.error("Duplicate definition of type \"" + typeNode.toString() + "\".");
        else
            typeTable.put(typeNode, type);
    }

    public Type get(TypeNode typeNode) {
        // Todo:
        return null;
    }
}