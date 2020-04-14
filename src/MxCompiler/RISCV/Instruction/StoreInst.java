package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Address;
import MxCompiler.RISCV.Operand.Register.Register;

public class StoreInst extends ASMInstruction {
    public enum ByteSize {
        sb, sw
    }

    private Register rs;
    private StoreInst.ByteSize byteSize;
    private Address addr;

    public StoreInst(BasicBlock basicBlock, Register rs, ByteSize byteSize, Address addr) {
        super(basicBlock);
        this.rs = rs;
        this.byteSize = byteSize;
        this.addr = addr;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
