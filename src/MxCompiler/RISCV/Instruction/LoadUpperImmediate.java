package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Immediate.Immediate;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

public class LoadUpperImmediate extends ASMInstruction {
    private VirtualRegister rd;
    private Immediate rs;

    public LoadUpperImmediate(BasicBlock basicBlock, VirtualRegister rd, Immediate rs) {
        super(basicBlock);
        this.rd = rd;
        this.rs = rs;

        this.rd.addDef(this);
        this.addDef(this.rd);
    }

    @Override
    public void addToUEVarAndVarKill(Set<VirtualRegister> UEVar, Set<VirtualRegister> varKill) {
        varKill.add(rd);
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
