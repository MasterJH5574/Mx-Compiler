package MxCompiler.Optim;

import MxCompiler.IR.Module;

abstract public class Pass {
    protected Module module;
    protected boolean changed;


    public Pass(Module module) {
        this.module = module;
    }

    abstract public boolean run();
}
