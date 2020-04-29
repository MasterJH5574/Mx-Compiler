package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Immediate.Immediate;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

public class LoadImmediate extends ASMInstruction {
    private VirtualRegister rd;
    private Immediate immediate;

    public LoadImmediate(BasicBlock basicBlock, VirtualRegister rd, Immediate immediate) {
        super(basicBlock);
        this.rd = rd;
        this.immediate = immediate;

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
