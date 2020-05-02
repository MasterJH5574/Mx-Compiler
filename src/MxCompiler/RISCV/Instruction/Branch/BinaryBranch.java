package MxCompiler.RISCV.Instruction.Branch;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

public class BinaryBranch extends Branch {
    public enum OpName {
        beq, bne, blt, bge, ble, bgt
    }

    private OpName op;
    private VirtualRegister rs2;

    public BinaryBranch(BasicBlock basicBlock, OpName op,
                        VirtualRegister rs1, VirtualRegister rs2, BasicBlock thenBlock) {
        super(basicBlock, rs1, thenBlock);
        this.op = op;
        this.rs2 = rs2;

        this.rs2.addUse(this);
        this.addUse(this.rs2);
    }

    @Override
    public void addToUEVarAndVarKill(Set<VirtualRegister> UEVar, Set<VirtualRegister> varKill) {
        super.addToUEVarAndVarKill(UEVar, varKill);
        if (!varKill.contains(rs2))
            UEVar.add(rs2);
    }

    @Override
    public void replaceUse(VirtualRegister oldVR, VirtualRegister newVR) {
        if (rs2 == oldVR)
            rs2 = newVR;
        super.replaceUse(oldVR, newVR);
    }

    @Override
    public String emitCode() {
        return "\t" + op.name() + "\t"
                + getRs1().emitCode() + ", " + rs2.emitCode() + ", " + getThenBlock().emitCode();
    }

    @Override
    public String toString() {
        return op + " " + getRs1() + ", " + rs2 + ", " + getThenBlock();
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
