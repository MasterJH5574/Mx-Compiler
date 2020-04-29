package MxCompiler.RISCV;

import MxCompiler.RISCV.Operand.Address.StackLocation;
import MxCompiler.RISCV.Operand.Register.PhysicalRegister;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StackFrame {
    private Function function;

    private Map<VirtualRegister, StackLocation> spillLocations;

    private ArrayList<StackLocation> formalParameterLocations; // Fetch from caller's stack frame.
    private Map<Function, ArrayList<StackLocation>> parameterLocation;

    public StackFrame(Function function) {
        this.function = function;

        spillLocations = new HashMap<>();

        formalParameterLocations = new ArrayList<>();
        parameterLocation = new HashMap<>();
    }

    public Map<VirtualRegister, StackLocation> getSpillLocations() {
        return spillLocations;
    }

    public void addFormalParameterLocation(StackLocation stackLocation) {
        formalParameterLocations.add(stackLocation);
    }

    public Map<Function, ArrayList<StackLocation>> getParameterLocation() {
        return parameterLocation;
    }
}
