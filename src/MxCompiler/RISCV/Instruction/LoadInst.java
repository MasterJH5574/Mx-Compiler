package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Address.Address;
import MxCompiler.RISCV.Operand.Register.Register;

import java.util.Set;

public class LoadInst extends ASMInstruction {
    public enum ByteSize {
        lb, lw
    }

    private Register rd;
    private ByteSize byteSize;
    private Address addr;

    public LoadInst(BasicBlock basicBlock, Register rd, ByteSize byteSize, Address addr) {
        super(basicBlock);
        this.rd = rd;
        this.byteSize = byteSize;
        this.addr = addr;

        this.rd.addDef(this);
        this.addDef(this.rd);
        this.addr.addBaseUse(this);
    }

    @Override
    public void addToUEVarAndVarKill(Set<Register> UEVar, Set<Register> varKill) {
        addr.addToUEVarAndVarKill(UEVar, varKill);
        varKill.add(rd);
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
