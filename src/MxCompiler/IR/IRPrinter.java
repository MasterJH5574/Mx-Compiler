package MxCompiler.IR;

import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.*;

import java.io.*;

public class IRPrinter implements IRVisitor {
    private OutputStream os;
    private PrintWriter writer;
    private String indent;

    public IRPrinter() {
        try {
            os = new FileOutputStream("test/test.ll");
            writer = new PrintWriter(os);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        indent = "    ";
    }

    public OutputStream getOs() {
        return os;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    private void print(String string) {
        writer.print(string);
    }

    private void println(String string) {
        writer.println(string);
    }

    @Override
    public void visit(Module module) {
        // ------ HEAD ------
        println("; ModuleID = 'code.txt'");
        println("source_filename = \"code.txt\"");
        println("target datalayout = \"e-m:e-i64:64-f80:128-n8:16:32:64-S128\"");
        println("target triple = \"x86_64-pc-linux-gnu\"");
        println("");

        // ------ STRUCTURE ------
        if (module.getStructureMap().size() > 0) {
            for (String name : module.getStructureMap().keySet())
                println(module.getStructureMap().get(name).structureToString());
            println("");
        }

        // ------ GLOBAL VARIABLE ------
        if (module.getGlobalVariableMap().size() > 0) {
            for (String name : module.getGlobalVariableMap().keySet())
                println(module.getGlobalVariableMap().get(name).definitionToString());
            println("");
        }

        // ------ EXTERNAL FUNCTION ------
        for (String name : module.getExternalFunctionMap().keySet())
            println(module.getExternalFunctionMap().get(name).declareToString());
        println("");

        for (String name : module.getFunctionMap().keySet()) {
            module.getFunctionMap().get(name).accept(this); // visit Function
            println("");
        }
    }

    @Override
    public void visit(Function function) {
        println(function.declareToString().replace("declare", "define") + " {");

        BasicBlock ptr = function.getEntranceBlock();
        while (ptr != null) {
            ptr.accept(this); // visit BasicBlock
            if (ptr.hasNext())
                println("");
            ptr = ptr.getNext();
        }

        println("}");
    }

    @Override
    public void visit(BasicBlock block) {
        print(block.getName() + ":");
        if (block.hasPredecessor()) {
            print(" ".repeat(50 - (block.getName().length() + 1)));
            print("; preds = ");
            int size = block.getPredecessors().size();
            int cnt = 0;
            for (BasicBlock predecessor : block.getPredecessors()) {
                print(predecessor.toString());
                if (++cnt != size)
                    print(", ");
            }
        }
        println("");

        IRInstruction ptr = block.getInstHead();
        while (ptr != null) {
            ptr.accept(this); // visit IRInstruction
            ptr = ptr.getInstNext();
        }
    }

    @Override
    public void visit(ReturnInst inst) {
        println(indent + inst.toString());
    }

    @Override
    public void visit(BranchInst inst) {
        println(indent + inst.toString());
    }

    @Override
    public void visit(BinaryOpInst inst) {
        println(indent + inst.toString());
    }

    @Override
    public void visit(AllocateInst inst) {
        println(indent + inst.toString());
    }

    @Override
    public void visit(LoadInst inst) {
        println(indent + inst.toString());
    }

    @Override
    public void visit(StoreInst inst) {
        println(indent + inst.toString());
    }

    @Override
    public void visit(GetElementPtrInst inst) {
        println(indent + inst.toString());
    }

    @Override
    public void visit(BitCastToInst inst) {
        println(indent + inst.toString());
    }

    @Override
    public void visit(IcmpInst inst) {
        println(indent + inst.toString());
    }

    @Override
    public void visit(PhiInst inst) {
        println(indent + inst.toString());
    }

    @Override
    public void visit(CallInst inst) {
        println(indent + inst.toString());
    }
}
