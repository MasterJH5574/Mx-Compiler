package MxCompiler.RISCV;

import MxCompiler.RISCV.Instruction.*;
import MxCompiler.RISCV.Instruction.BinaryInst.ITypeBinary;
import MxCompiler.RISCV.Instruction.BinaryInst.RTypeBinary;
import MxCompiler.RISCV.Instruction.Branch.BinaryBranch;
import MxCompiler.RISCV.Instruction.Branch.UnaryBranch;
import MxCompiler.RISCV.Operand.GlobalVariable;

public interface ASMVisitor {
    void visit(Module module);
    void visit(Function function);
    void visit(BasicBlock block);

    void visit(GlobalVariable gv);

    void visit(MoveInst inst);
    void visit(UnaryInst inst);
    void visit(ITypeBinary inst);
    void visit(RTypeBinary inst);

    void visit(LoadAddressInst inst);
    void visit(LoadImmediate inst);
    void visit(LoadUpperImmediate inst);

    void visit(LoadInst inst);
    void visit(StoreInst inst);

    void visit(JumpInst inst);
    void visit(BinaryBranch inst);
    void visit(UnaryBranch inst);
    void visit(CallInst inst);
    void visit(ReturnInst inst);
}
