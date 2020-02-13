package MxCompiler.IR;

import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.*;

public interface IRVisitor {
    // ------ Module ------
    void visit(Module module);

    // ------ Function ------
    void visit(Function function);

    // ------ BasicBlock ------
    void visit(BasicBlock block);

    // ------ Instruction ------
    void visit(ReturnInst inst);
    void visit(BranchInst inst);
    void visit(BinaryOpInst inst);
    void visit(AllocateInst inst);
    void visit(LoadInst inst);
    void visit(StoreInst inst);
    void visit(GetElementPtrInst inst);
    void visit(BitCastToInst inst);
    void visit(IcmpInst inst);
    void visit(PhiInst inst);
    void visit(CallInst inst);

    // ------ Type System ------
    void visit(VoidType type);
    void visit(FunctionType type);
    void visit(IntegerType type);
    void visit(PointerType type);
    void visit(ArrayType type);
    void visit(StructureType type);

    // ------ Operand ------
    void visit(GlobalVariable globalVariable);
    void visit(Register register);
    void visit(Parameter parameter);
    void visit(ConstInt constInt);
    void visit(ConstBool constBool);
    void visit(ConstString constString);
    void visit(ConstNull constNull);
}
