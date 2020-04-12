package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Address;
import MxCompiler.RISCV.Operand.GlobalVariable;
import MxCompiler.RISCV.Operand.Register.Register;

public class StoreInst extends ASMInstruction {
    public enum ByteSize {
        sb, sw
    }
    public enum StoreType {
        globalVariable, address
    }

    private Register rs; // value to be stored
    private Register rt; // reserved assistant register
    private StoreInst.ByteSize byteSize;
    private StoreInst.StoreType loadType;
    private GlobalVariable globalVariable;
    private Address addr;

    public StoreInst(BasicBlock basicBlock, Register rs, Register rt,
                     ByteSize byteSize, StoreType loadType, GlobalVariable globalVariable, Address addr) {
        super(basicBlock);
        this.rs = rs;
        this.rt = rt;
        this.byteSize = byteSize;
        this.loadType = loadType;
        this.globalVariable = globalVariable;
        this.addr = addr;
    }
}
