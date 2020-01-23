package MxCompiler;

import MxCompiler.AST.ProgramNode;
import MxCompiler.Frontend.ASTBuilder;
import MxCompiler.Parser.MxLexer;
import MxCompiler.Parser.MxParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.FileInputStream;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) {
        try {
            InputStream inputStream = new FileInputStream("code.txt");

            CharStream input = CharStreams.fromStream(inputStream);
            MxLexer lexer = new MxLexer(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            MxParser parser = new MxParser(tokens);
            ParseTree parseTreeEntrance = parser.program();

            ASTBuilder astBuilder = new ASTBuilder();
            ProgramNode astRoot = (ProgramNode) astBuilder.visit(parseTreeEntrance);
            System.out.println("Build AST successfully!");
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
    }
}
