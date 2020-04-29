package MxCompiler.RISCV.Operand.Register;

import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Operand.ASMOperand;

import java.util.HashMap;
import java.util.Map;

public class Register extends ASMOperand {
    private Map<ASMInstruction, Integer> use;
    private Map<ASMInstruction, Integer> def;

    public Register() {
        use = new HashMap<>();
        def = new HashMap<>();
    }

    public void addUse(ASMInstruction instruction) {
        if (use.containsKey(instruction))
            use.replace(instruction, use.get(instruction) + 1);
        else
            use.put(instruction, 1);
    }

    public void addDef(ASMInstruction instruction) {
        if (def.containsKey(instruction))
            def.replace(instruction, def.get(instruction) + 1);
        else
            def.put(instruction, 1);
    }

    public Map<ASMInstruction, Integer> getUse() {
        return use;
    }

    public Map<ASMInstruction, Integer> getDef() {
        return def;
    }
}
