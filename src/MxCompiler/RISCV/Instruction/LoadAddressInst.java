package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.GlobalVariable;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

public class LoadAddressInst extends ASMInstruction {
    private VirtualRegister rd;
    private GlobalVariable globalVariable;

    public LoadAddressInst(BasicBlock basicBlock, VirtualRegister rd, GlobalVariable globalVariable) {
        super(basicBlock);
        this.rd = rd;
        this.globalVariable = globalVariable;

        this.rd.addDef(this);
        this.addDef(this.rd);
    }

    @Override
    public void addToUEVarAndVarKill(Set<VirtualRegister> UEVar, Set<VirtualRegister> varKill) {
        varKill.add(rd);
    }

    @Override
    public void replaceDef(VirtualRegister oldVR, VirtualRegister newVR) {
        assert rd == oldVR;
        rd = newVR;
        super.replaceDef(oldVR, newVR);
    }

    @Override
    public String emitCode() {
        return "\tla\t" + rd.emitCode() + ", " + globalVariable.getName();
    }

    @Override
    public String toString() {
        return "la " + rd + ", " + globalVariable.getName();
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
