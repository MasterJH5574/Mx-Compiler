package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.ASMVisitor;
import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Function;
import MxCompiler.RISCV.Operand.Register.PhysicalRegister;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.Map;

public class CallInst extends ASMInstruction {
    private Function callee;

    public CallInst(BasicBlock basicBlock, Function callee) {
        super(basicBlock);
        this.callee = callee;

        for (String name : PhysicalRegister.callerSavePRNames) {
            PhysicalRegister.vrs.get(name).addDef(this);
            this.addDef(PhysicalRegister.vrs.get(name));
        }
    }

    @Override
    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
