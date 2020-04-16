package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Function;
import MxCompiler.RISCV.Operand.Register.PhysicalRegister;

import java.util.Map;

public class CallInst extends ASMInstruction {
    private Function callee;
    private Map<String, PhysicalRegister> unsavedCallerSaveRegs;
    private ASMInstruction firstInst;
    private ASMInstruction lastInst;

    public CallInst(BasicBlock basicBlock, Function callee, Map<String, PhysicalRegister> unsavedCallerSaveRegs) {
        super(basicBlock);
        this.callee = callee;
        this.unsavedCallerSaveRegs = unsavedCallerSaveRegs;

        firstInst = null;
        lastInst = null;
    }

    public void setFirstInst(ASMInstruction firstInst) {
        this.firstInst = firstInst;
    }

    public void setLastInst(ASMInstruction lastInst) {
        this.lastInst = lastInst;
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
