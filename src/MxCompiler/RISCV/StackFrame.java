package MxCompiler.RISCV;

import MxCompiler.RISCV.Operand.Address.StackLocation;
import MxCompiler.RISCV.Operand.Register.PhysicalRegister;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class StackFrame {
    private Function function;

    private StackLocation raLocation;
    private Map<PhysicalRegister, StackLocation> calleeSaveLocations;
    private Map<VirtualRegister, StackLocation> spillLocations;

    private ArrayList<StackLocation> formalParameterLocations; // Fetch from caller's stack frame.
    private Map<Function, ArrayList<StackLocation>> parameterLocation;

    public StackFrame(Function function, StackLocation raLocation) {
        this.function = function;

        this.raLocation = raLocation;
        calleeSaveLocations = new HashMap<>();
        spillLocations = new HashMap<>();

        formalParameterLocations = new ArrayList<>();
        parameterLocation = new HashMap<>();
    }
}
