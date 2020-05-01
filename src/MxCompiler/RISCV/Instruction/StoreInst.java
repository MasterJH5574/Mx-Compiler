package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Address.Address;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

public class StoreInst extends ASMInstruction {
    public enum ByteSize {
        sb, sw
    }

    private VirtualRegister rs;
    private StoreInst.ByteSize byteSize;
    private Address addr;

    public StoreInst(BasicBlock basicBlock, VirtualRegister rs, ByteSize byteSize, Address addr) {
        super(basicBlock);
        this.rs = rs;
        this.byteSize = byteSize;
        this.addr = addr;

        this.rs.addUse(this);
        this.addUse(this.rs);
        this.addr.addBaseUse(this);
    }

    public Address getAddr() {
        return addr;
    }

    @Override
    public void addToUEVarAndVarKill(Set<VirtualRegister> UEVar, Set<VirtualRegister> varKill) {
        if (!varKill.contains(rs))
            UEVar.add(rs);
        addr.addToUEVarAndVarKill(UEVar, varKill);
    }

    @Override
    public void replaceUse(VirtualRegister oldVR, VirtualRegister newVR) {
        if (rs == oldVR)
            rs = newVR;
        addr.replaceUse(oldVR, newVR);
        super.replaceUse(oldVR, newVR);
    }

    @Override
    public String emitCode() {
        return "\t" + byteSize.name() + "\t" + rs.emitCode() + ", " + addr.emitCode();
    }

    @Override
    public String toString() {
        return byteSize + " " + rs + ", " + addr;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
