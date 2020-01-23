package MxCompiler.Utilities;

import org.antlr.v4.runtime.Token;

public class Location {
    private int line, column;

    public Location(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public Location(Token token) {
        this.line = token.getLine();
        this.column = token.getCharPositionInLine();
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String toString() {
        return "Line " + line + ":" + column;
    }
}
