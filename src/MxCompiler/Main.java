package MxCompiler;

import MxCompiler.AST.ProgramNode;
import MxCompiler.Frontend.ASTBuilder;
import MxCompiler.Frontend.Checker;
import MxCompiler.Parser.MxErrorListener;
import MxCompiler.Parser.MxLexer;
import MxCompiler.Parser.MxParser;
import MxCompiler.Utilities.CompilationError;
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
        String failed = "Compilation Failed.";
        String success = "Compilation Success!";

        InputStream inputStream;
        CharStream input;
        MxLexer lexer;
        CommonTokenStream tokens;
        MxParser parser;
        ParseTree parseTreeEntrance;
        try {
            inputStream = new FileInputStream("code.txt");
            // inputStream = System.in;
            input = CharStreams.fromStream(inputStream);
        } catch (Exception e) {
            errorHandler.error("Cannot open file \"code.txt\".");
            errorHandler.print();
            System.out.println(failed);
            throw new RuntimeException();
        }

        lexer = new MxLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new MxErrorListener(errorHandler));
        tokens = new CommonTokenStream(lexer);
        parser = new MxParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new MxErrorListener(errorHandler));
        parseTreeEntrance = parser.program();
        if (errorHandler.hasError()) {
            errorHandler.print();
            System.out.println(failed);
            throw new RuntimeException();
        }


        ASTBuilder astBuilder = new ASTBuilder(errorHandler);
        ProgramNode astRoot;
        astRoot = (ProgramNode) astBuilder.visit(parseTreeEntrance); // throws no error

        Checker semanticChecker = new Checker(errorHandler);
        try {
            astRoot.accept(semanticChecker);
        } catch (CompilationError e) {
            errorHandler.print();
            System.out.println(failed);
            throw new RuntimeException();
        }

        errorHandler.print();
        if (errorHandler.hasError()) {
            System.out.println(failed);
            throw new RuntimeException();
        }

        System.out.println(success);
    }
}
