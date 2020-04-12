package MxCompiler.RISCV.Instruction;

import MxCompiler.RISCV.BasicBlock;
import MxCompiler.RISCV.Operand.Address;
import MxCompiler.RISCV.Operand.GlobalVariable;
import MxCompiler.RISCV.Operand.Register.Register;

public class LoadInst extends ASMInstruction {
    public enum ByteSize {
        lb, lw
    }
    public enum LoadType {
        globalVariable, address
    }

    private Register rd;
    private ByteSize byteSize;
    private LoadType loadType;
    private GlobalVariable globalVariable;
    private Address addr;

    public LoadInst(BasicBlock basicBlock, Register rd, ByteSize byteSize,
                    LoadType loadType, GlobalVariable globalVariable, Address addr) {
        super(basicBlock);
        this.rd = rd;
        this.byteSize = byteSize;
        this.loadType = loadType;
        this.globalVariable = globalVariable;
        this.addr = addr;
    }
}
