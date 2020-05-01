package MxCompiler.Backend;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Function;
import MxCompiler.RISCV.Instruction.*;
import MxCompiler.RISCV.Instruction.BinaryInst.ITypeBinary;
import MxCompiler.RISCV.Instruction.BinaryInst.RTypeBinary;
import MxCompiler.RISCV.Instruction.Branch.BinaryBranch;
import MxCompiler.RISCV.Instruction.Branch.UnaryBranch;
import MxCompiler.RISCV.Module;
import MxCompiler.RISCV.Operand.GlobalVariable;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CodeEmitter implements ASMVisitor {
    private OutputStream os;
    private PrintWriter writer;
    private String indent;

    private int functionCnt;

    public CodeEmitter(String filename) {
        try {
            os = new FileOutputStream(filename);
            writer = new PrintWriter(os);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }

        indent = "\t";
    }

    public void run(Module module) {
        module.accept(this);

        try {
            writer.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    private void print(String string) {
        writer.print(string);
    }

    private void println(String string) {
        writer.println(string);
    }

    @Override
    public void visit(Module module) {
        println(indent + ".text");
        println("");

        functionCnt = 0;
        for (Function function : module.getFunctionMap().values())
            function.accept(this);

        println("");

        for (GlobalVariable gv : module.getGlobalVariableMap().values())
            gv.accept(this);
    }

    @Override
    public void visit(Function function) {
        print(indent + ".globl" + indent + function.getName());
        print(" ".repeat(Integer.max(1, 24 - function.getName().length())));
        println("# -- Begin function " + function.getName());
        println(indent + ".p2align" + indent + "2");

        print(function.getName() + ":" + " ".repeat(Integer.max(1, 31 - function.getName().length())));
        println("# @" + function.getName());

        ArrayList<BasicBlock> blocks = function.getBlocks();
        for (BasicBlock block : blocks)
            block.accept(this);

        println(".Lfunc_end" + functionCnt + ":");
        println(" ".repeat(40) + "# -- End function");
        println("");

        functionCnt++;
    }

    @Override
    public void visit(BasicBlock block) {
        String name = block.getAsmName();
        println(name + ":" + " ".repeat(40 - 1 - name.length()) + "# " + block.getName());

        ASMInstruction ptr = block.getInstHead();
        while (ptr != null) {
            println(ptr.emitCode());
            ptr = ptr.getNextInst();
        }
    }

    @Override
    public void visit(GlobalVariable gv) {
        if (!gv.isString()) {
            println(indent + ".globl" + indent + gv.getName());
            println(indent + ".p2align" + indent + "2");
        }
        println(gv.getName() + ":");
        println(gv.emitCode());
        println("");
    }

    @Override
    public void visit(MoveInst inst) {

    }

    @Override
    public void visit(UnaryInst inst) {

    }

    @Override
    public void visit(ITypeBinary inst) {

    }

    @Override
    public void visit(RTypeBinary inst) {

    }

    @Override
    public void visit(LoadAddressInst inst) {

    }

    @Override
    public void visit(LoadImmediate inst) {

    }

    @Override
    public void visit(LoadUpperImmediate inst) {

    }

    @Override
    public void visit(LoadInst inst) {

    }

    @Override
    public void visit(StoreInst inst) {

    }

    @Override
    public void visit(JumpInst inst) {

    }

    @Override
    public void visit(BinaryBranch inst) {

    }

    @Override
    public void visit(UnaryBranch inst) {

    }

    @Override
    public void visit(CallInst inst) {

    }

    @Override
    public void visit(ReturnInst inst) {

    }
}
