package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Register.Register;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Set;

abstract public class ASMInstruction {
    private BasicBlock basicBlock;
    private ASMInstruction prevInst;
    private ASMInstruction nextInst;

    public ASMInstruction(BasicBlock basicBlock) {
        this.basicBlock = basicBlock;
        prevInst = null;
        nextInst = null;
    }

    public void setPrevInst(ASMInstruction prevInst) {
        this.prevInst = prevInst;
    }

    public ASMInstruction getNextInst() {
        return nextInst;
    }

    public void setNextInst(ASMInstruction nextInst) {
        this.nextInst = nextInst;
    }

    public void addToUEVarAndVarKill(Set<Register> UEVar, Set<Register> varKill) {

    }

    abstract public void accept(ASMVisitor visitor);
}
