package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Function;

public class CallInst extends ASMInstruction {
    private Function function;

    public CallInst(BasicBlock basicBlock, Function function) {
        super(basicBlock);
        this.function = function;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
