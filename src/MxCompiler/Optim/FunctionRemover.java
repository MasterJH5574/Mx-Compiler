package MxCompiler.Optim;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.Instruction.CallInst;
import MxCompiler.IR.Instruction.IRInstruction;
import MxCompiler.IR.Module;

import java.util.*;

public class FunctionRemover extends Pass {
    public FunctionRemover(Module module) {
        super(module);
    }

    @Override
    public boolean run() {
        Function mainFunction = module.getFunctionMap().get("main");
        Set<Function> functionSet = new HashSet<>();
        Queue<Function> queue = new LinkedList<>();
        functionSet.add(mainFunction);
        queue.offer(mainFunction);

        while (!queue.isEmpty()) {
            Function function = queue.poll();
            for (BasicBlock block : function.getBlocks()) {
                IRInstruction ptr = block.getInstHead();
                while (ptr != null) {
                    if (ptr instanceof CallInst) {
                        Function callee = ((CallInst) ptr).getFunction();
                        if (!functionSet.contains(callee) && !module.getExternalFunctionMap().containsValue(callee)) {
                            functionSet.add(callee);
                            queue.offer(callee);
                        }
                    }
                    ptr = ptr.getInstNext();
                }
            }
        }

        changed = false;
        Set<String> removeSet = new HashSet<>();
        for (Map.Entry<String, Function> entry : module.getFunctionMap().entrySet()) {
            if (!functionSet.contains(entry.getValue()))
                removeSet.add(entry.getKey());
        }
        for (String name : removeSet)
            module.getFunctionMap().remove(name);
        return changed;
    }
}
