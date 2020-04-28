package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Register.Register;

import java.util.Set;

public class MoveInst extends ASMInstruction {
    private Register rd;
    private Register rs;

    public MoveInst(BasicBlock basicBlock, Register rd, Register rs) {
        super(basicBlock);
        this.rd = rd;
        this.rs = rs;

        this.rs.addUse(this);
        this.rd.addDef(this);
        this.addUse(this.rs);
        this.addDef(this.rd);
    }

    @Override
    public void addToUEVarAndVarKill(Set<Register> UEVar, Set<Register> varKill) {
        if (!varKill.contains(rs))
            UEVar.add(rs);
        varKill.add(rd);
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
