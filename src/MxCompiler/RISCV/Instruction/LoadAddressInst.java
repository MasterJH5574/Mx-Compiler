package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.GlobalVariable;
import MxCompiler.RISCV.Operand.Register.Register;

public class LoadAddressInst extends ASMInstruction {
    private Register rd;
    private GlobalVariable globalVariable;

    public LoadAddressInst(BasicBlock basicBlock, Register rd, GlobalVariable globalVariable) {
        super(basicBlock);
        this.rd = rd;
        this.globalVariable = globalVariable;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
