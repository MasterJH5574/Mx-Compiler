package MxCompiler;

import MxCompiler.AST.ProgramNode;
import MxCompiler.Backend.CodeEmitter;
import MxCompiler.Backend.InstructionSelector;
import MxCompiler.Backend.PeepholeOptimization;
import MxCompiler.Backend.RegisterAllocator;
import MxCompiler.Frontend.ASTBuilder;
import MxCompiler.Frontend.Checker;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRBuilder;
import MxCompiler.IR.IRPrinter;
import MxCompiler.IR.Module;
import MxCompiler.Optim.*;
import MxCompiler.Optim.LoopOptim.LICM;
import MxCompiler.Optim.LoopOptim.LoopAnalysis;
import MxCompiler.Optim.SSA.SSAConstructor;
import MxCompiler.Optim.SSA.SSADestructor;
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
//            inputStream = System.in;
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


        IRBuilder irBuilder = new IRBuilder(semanticChecker.getGlobalScope(),
                semanticChecker.getTypeTable(),
                errorHandler);
        try {
            astRoot.accept(irBuilder);
        } catch (CompilationError e) {
            errorHandler.print();
            System.out.println(failed);
            throw new RuntimeException();
        }
        if (errorHandler.hasError()) {
            errorHandler.print();
            System.out.println(failed);
            throw new RuntimeException();
        }


        // ------ Optimizations ------
        Module module = irBuilder.getModule();

        // ------ Simplify CFG, construct Dominator Tree & run SSAConstructor(mem2reg) ------
        CFGSimplifier cfgSimplifier = new CFGSimplifier(module);
        cfgSimplifier.run();
        DominatorTreeConstructor dominatorTreeConstructor = new DominatorTreeConstructor(module);
        dominatorTreeConstructor.run();
        SSAConstructor ssaConstructor = new SSAConstructor(module);
        ssaConstructor.run();

        for (Function function : module.getFunctionMap().values()) {
            if (function.isNotFunctional()) {
//                new IRPrinter("test/test.ll").run(module);
//                finalPrint(errorHandler);
                return;
            }
        }

        Andersen andersen = new Andersen(module);
        SideEffectChecker sideEffectChecker = new SideEffectChecker(module);
        LoopAnalysis loopAnalysis = new LoopAnalysis(module);
        DeadCodeEliminator deadCodeEliminator = new DeadCodeEliminator(module, sideEffectChecker, loopAnalysis);
        SCCP sccp = new SCCP(module);
        CSE cse = new CSE(module, andersen, sideEffectChecker);
        LICM licm = new LICM(module, loopAnalysis, sideEffectChecker, andersen);
        InstructionCombiner instructionCombiner = new InstructionCombiner(module);
        InlineExpander inlineExpander = new InlineExpander(module);
        FunctionRemover functionRemover = new FunctionRemover(module);
        while (true) {
            boolean changed;
            dominatorTreeConstructor.run();
            changed = sccp.run();
            changed |= deadCodeEliminator.run();
            changed |= cfgSimplifier.run();
            andersen.run();
            changed |= cse.run();
            loopAnalysis.run();
            changed |= licm.run();
            changed |= inlineExpander.run();
            changed |= instructionCombiner.run();
            changed |= cfgSimplifier.run();
            changed |= functionRemover.run();

            if (!changed)
                break;
        }

        // Print LLVM IR.
//        new IRPrinter("test/test.ll").run(module);

        new SSADestructor(module).run();

        // Print IR after SSA destruction.
//        new IRPrinter("test/postIR.ll").run(module);

        InstructionSelector instructionSelector = new InstructionSelector();
        module.accept(instructionSelector);

        MxCompiler.RISCV.Module ASMModule = instructionSelector.getASMModule();
//        new CodeEmitter("test/preASM.s", false).run(ASMModule);

        dominatorTreeConstructor.run();
        loopAnalysis.run();

        new RegisterAllocator(ASMModule, loopAnalysis).run();
        new PeepholeOptimization(ASMModule).run();
//        new CodeEmitter("test/test.s", true).run(ASMModule);
        new CodeEmitter("output.s", true).run(ASMModule);

//        finalPrint(errorHandler);
    }

    static private void finalPrint(ErrorHandler errorHandler) {
        String failed = "Compilation Failed.";
        String success = "Compilation Success!";

        errorHandler.print();
        if (errorHandler.hasError()) {
            System.out.println(failed);
            throw new RuntimeException();
        }

        System.out.println(success);
    }
}
