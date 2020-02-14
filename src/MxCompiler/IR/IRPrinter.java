package MxCompiler.IR;

import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.*;

import java.io.File;
import java.io.FileWriter;

public class IRPrinter implements IRVisitor {
    private Module module;

    private FileWriter writer;
    private String indent;

    public IRPrinter(Module module) {
        File file = new File("test.ll");
        try {
            boolean createResult = file.createNewFile();
            if (!createResult)
                throw new RuntimeException();
            writer = new FileWriter(file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        indent = "    ";

        this.module = module;
        this.module.accept(this);
    }

    private void print(String string) {
        try {
            writer.write(string);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void println(String string) {
        try {
            writer.write(string + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void visit(Module module) {
        // ------ HEAD ------
        println("; ModuleID = 'test.txt'");
        println("source_filename = \"test.txt\"");
        println("target datalayout = \"e-m:e-i64:64-f80:128-n8:16:32:64-S128\"");
        println("target triple = \"x86_64-pc-linux-gnu\"");
        println("");

        // ------ STRUCTURE ------
        for (String name : module.getStructureMap().keySet())
            println(module.getStructureMap().get(name).structureToString());
        println("");

        // ------ GLOBAL VARIABLE ------
        for (String name : module.getGlobalVariableMap().keySet())
            println(module.getGlobalVariableMap().get(name).definitionToString());
        println("");

        // ------ EXTERNAL FUNCTION ------
        for (String name : module.getExternalFunctionMap().keySet())
            println(module.getExternalFunctionMap().get(name).declareToString());
        println("");

        for (String name : module.getFunctionMap().keySet())
            module.getFunctionMap().get(name).accept(this); // visit Function
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
            for (int i = 0; i < block.getPredecessors().size(); i++) {
                print(block.getPredecessors().get(i).toString());
                if (i != block.getPredecessors().size() - 1)
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

    @Override
    public void visit(VoidType type) {

    }

    @Override
    public void visit(FunctionType type) {

    }

    @Override
    public void visit(IntegerType type) {

    }

    @Override
    public void visit(PointerType type) {

    }

    @Override
    public void visit(ArrayType type) {

    }

    @Override
    public void visit(StructureType type) {

    }

    @Override
    public void visit(GlobalVariable globalVariable) {

    }

    @Override
    public void visit(Register register) {

    }

    @Override
    public void visit(Parameter parameter) {

    }

    @Override
    public void visit(ConstInt constInt) {

    }

    @Override
    public void visit(ConstBool constBool) {

    }

    @Override
    public void visit(ConstString constString) {

    }

    @Override
    public void visit(ConstNull constNull) {

    }
}
