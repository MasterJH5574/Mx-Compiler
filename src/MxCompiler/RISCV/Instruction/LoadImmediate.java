package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Immediate.Immediate;
import MxCompiler.RISCV.Operand.Register.Register;

import java.util.Set;

public class LoadImmediate extends ASMInstruction {
    private Register rd;
    private Immediate immediate;

    public LoadImmediate(BasicBlock basicBlock, Register rd, Immediate immediate) {
        super(basicBlock);
        this.rd = rd;
        this.immediate = immediate;

        this.rd.addDef(this);
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
