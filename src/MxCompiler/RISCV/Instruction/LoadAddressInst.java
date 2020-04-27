package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.GlobalVariable;
import MxCompiler.RISCV.Operand.Register.Register;

import java.util.Set;

public class LoadAddressInst extends ASMInstruction {
    private Register rd;
    private GlobalVariable globalVariable;

    public LoadAddressInst(BasicBlock basicBlock, Register rd, GlobalVariable globalVariable) {
        super(basicBlock);
        this.rd = rd;
        this.globalVariable = globalVariable;

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
