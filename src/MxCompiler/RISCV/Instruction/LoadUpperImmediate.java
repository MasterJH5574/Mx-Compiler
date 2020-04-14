package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Immediate;
import MxCompiler.RISCV.Operand.Register.Register;

public class LoadUpperImmediate extends ASMInstruction {
    private Register rd;
    private Immediate rs;

    public LoadUpperImmediate(BasicBlock basicBlock, Register rd, Immediate rs) {
        super(basicBlock);
        this.rd = rd;
        this.rs = rs;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
