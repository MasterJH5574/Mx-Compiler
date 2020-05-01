package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;

public class ReturnInst extends ASMInstruction {
    public ReturnInst(BasicBlock basicBlock) {
        super(basicBlock);
    }

    @Override
    public String emitCode() {
        return "\tret";
    }

    @Override
    public String toString() {
        return "ret";
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
