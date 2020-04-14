package MxCompiler.Backend;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.GlobalVariable;
import MxCompiler.IR.Operand.Parameter;
import MxCompiler.RISCV.Operand.Address.StackLocation;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;
import MxCompiler.RISCV.StackFrame;

import java.util.ArrayList;

public class InstructionSelector implements IRVisitor {
    private MxCompiler.RISCV.Module ASMModule;

    private MxCompiler.RISCV.Function currentFunction;
    private MxCompiler.RISCV.BasicBlock currentBlock;

    public InstructionSelector() {
        ASMModule = new MxCompiler.RISCV.Module();
        currentFunction = null;
        currentBlock = null;
    }

    @Override
    public void visit(Module module) {
        for (GlobalVariable IRGlobalVariable : module.getGlobalVariableMap().values()) {
            String name = IRGlobalVariable.getName();
            ASMModule.getGlobalVariableMap().put(name, new MxCompiler.RISCV.Operand.GlobalVariable(name));
        }
        for (Function IRExternalFunction : module.getExternalFunctionMap().values()) {
            String name = IRExternalFunction.getName();
            ASMModule.getExternalFunctionMap().put(name, new MxCompiler.RISCV.Function(ASMModule, name, null));
        }

        for (Function IRFunction : module.getFunctionMap().values())
            IRFunction.accept(this);
    }

    @Override
    public void visit(Function function) {
        String functionName = function.getName();
        currentFunction = new MxCompiler.RISCV.Function(ASMModule, functionName, function);
        ASMModule.getFunctionMap().put(functionName, currentFunction);

        StackFrame stackFrame = new StackFrame(currentFunction, new StackLocation(".ra"));
        currentFunction.setStackFrame(stackFrame);

        currentBlock = currentFunction.getEntranceBlock();
        ArrayList<Parameter> IRParameters = function.getParameters();
        for (int i = 0; i < Integer.min(IRParameters.size(), 8); i++) {
            Parameter parameter = IRParameters.get(i);
            VirtualRegister vr = currentFunction.getSymbolTable().getVR(parameter.getName());
//            currentBlock.addInstruction();
        }
        // Todo: symbolTable finished, move parameters
    }

    @Override
    public void visit(BasicBlock block) {

    }

    @Override
    public void visit(ReturnInst inst) {

    }

    @Override
    public void visit(BranchInst inst) {

    }

    @Override
    public void visit(BinaryOpInst inst) {

    }

    @Override
    public void visit(AllocateInst inst) {

    }

    @Override
    public void visit(LoadInst inst) {

    }

    @Override
    public void visit(StoreInst inst) {

    }

    @Override
    public void visit(GetElementPtrInst inst) {

    }

    @Override
    public void visit(BitCastToInst inst) {

    }

    @Override
    public void visit(IcmpInst inst) {

    }

    @Override
    public void visit(PhiInst inst) {

    }

    @Override
    public void visit(CallInst inst) {

    }

    @Override
    public void visit(MoveInst inst) {

    }
}
