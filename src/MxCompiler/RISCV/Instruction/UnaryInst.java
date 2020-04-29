package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

public class UnaryInst extends ASMInstruction {
    public enum OpName {
        seqz, snez, sltz, sgtz
    }

    private OpName op;
    private VirtualRegister rs;
    private VirtualRegister rd;

    public UnaryInst(BasicBlock basicBlock, OpName op, VirtualRegister rs, VirtualRegister rd) {
        super(basicBlock);
        this.op = op;
        this.rs = rs;
        this.rd = rd;

        this.rs.addUse(this);
        this.rd.addDef(this);
        this.addUse(this.rs);
        this.addUse(this.rd);
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
