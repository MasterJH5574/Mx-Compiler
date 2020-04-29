package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

public class MoveInst extends ASMInstruction {
    private VirtualRegister rd;
    private VirtualRegister rs;

    public MoveInst(BasicBlock basicBlock, VirtualRegister rd, VirtualRegister rs) {
        super(basicBlock);
        this.rd = rd;
        this.rs = rs;

        this.rs.addUse(this);
        this.rd.addDef(this);
        this.addUse(this.rs);
        this.addDef(this.rd);
    }

    public VirtualRegister getRd() {
        return rd;
    }

    public VirtualRegister getRs() {
        return rs;
    }

    @Override
    public void addToUEVarAndVarKill(Set<VirtualRegister> UEVar, Set<VirtualRegister> varKill) {
        if (!varKill.contains(rs))
            UEVar.add(rs);
        varKill.add(rd);
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
