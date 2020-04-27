package MxCompiler.Backend;

import MxCompiler.RISCV.Module;

abstract public class ASMPass {
    protected Module module;

    public ASMPass(Module module) {
        this.module = module;
    }

    abstract public void run();
}
