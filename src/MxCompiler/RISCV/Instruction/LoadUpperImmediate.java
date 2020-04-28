package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Immediate.Immediate;
import MxCompiler.RISCV.Operand.Register.Register;

import java.util.Set;

public class LoadUpperImmediate extends ASMInstruction {
    private Register rd;
    private Immediate rs;

    public LoadUpperImmediate(BasicBlock basicBlock, Register rd, Immediate rs) {
        super(basicBlock);
        this.rd = rd;
        this.rs = rs;

        this.rd.addDef(this);
        this.addDef(this.rd);
    }

    @Override
    public void addToUEVarAndVarKill(Set<Register> UEVar, Set<Register> varKill) {
        varKill.add(rd);
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
