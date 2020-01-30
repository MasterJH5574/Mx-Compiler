package MxCompiler;

import MxCompiler.AST.ProgramNode;
import MxCompiler.Frontend.ASTBuilder;
import MxCompiler.Parser.MxLexer;
import MxCompiler.Parser.MxParser;
import MxCompiler.Utilities.ErrorHandler;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        ErrorHandler errorHandler = new ErrorHandler();

        InputStream inputStream;
        CharStream input;
        try {
            inputStream = new FileInputStream("code.txt");
            input = CharStreams.fromStream(inputStream);
        } catch (Exception e) {
            errorHandler.error("Cannot open file \"code.txt\".");
            return;
        }

        MxLexer lexer = new MxLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MxParser parser = new MxParser(tokens);
        ParseTree parseTreeEntrance = parser.program();

        ASTBuilder astBuilder = new ASTBuilder(errorHandler);
        ProgramNode astRoot = null;
        try {
            astRoot = (ProgramNode) astBuilder.visit(parseTreeEntrance);
        } catch (Exception exception) {
            System.out.println("Compilation error.");
            errorHandler.print();
            return;
        }

        if (errorHandler.hasError())
            System.out.println("Compilation Error.");
        else
            System.out.println("Build AST successfully!");
        errorHandler.print();
    }
}
