package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;

public class ReturnInst extends ASMInstruction {
    public ReturnInst(BasicBlock basicBlock) {
        super(basicBlock);
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
