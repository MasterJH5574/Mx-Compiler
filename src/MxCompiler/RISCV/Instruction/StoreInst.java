package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Address.Address;
import MxCompiler.RISCV.Operand.Register.Register;

import java.util.Set;

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

        this.rs.addUse(this);
        this.addUse(this.rs);
        this.addr.addBaseUse(this);
    }

    @Override
    public void addToUEVarAndVarKill(Set<Register> UEVar, Set<Register> varKill) {
        if (!varKill.contains(rs))
            UEVar.add(rs);
        addr.addToUEVarAndVarKill(UEVar, varKill);
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
