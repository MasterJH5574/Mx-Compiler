package MxCompiler.RISCV;

public class Function {
    private Module module;

    private String name;

    private BasicBlock entranceBlock;
    private BasicBlock exitBlock;

    public Function(Module module, String name) {
        this.module = module;
        this.name = name;

        entranceBlock = null;
        exitBlock = null;
    }

    public void addBasicBlock(BasicBlock block) {
        if (entranceBlock == null)
            entranceBlock = block;
        else
            exitBlock.appendBlock(block);
        exitBlock = block;
    }

    public void accept(ASMVisitor visitor) {
        visitor.visit(this);
    }
}
