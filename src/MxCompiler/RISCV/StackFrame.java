package MxCompiler.RISCV;

import MxCompiler.RISCV.Operand.Address.StackLocation;
import MxCompiler.RISCV.Operand.Register.PhysicalRegister;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StackFrame {
    private Function function;

    private StackLocation raLocation; // raLocation == null  --->  There is no "call" in the function.
                                      // raLocation != null  --->  There is at least one "call" in the function.
    private Map<PhysicalRegister, StackLocation> calleeSaveLocations;
    private Map<VirtualRegister, StackLocation> spillLocations;

    private ArrayList<StackLocation> formalParameterLocations; // Fetch from caller's stack frame.
    private Map<Function, ArrayList<StackLocation>> parameterLocation;

    public StackFrame(Function function) {
        this.function = function;

        this.raLocation = null;
        calleeSaveLocations = new HashMap<>();
        spillLocations = new HashMap<>();

        formalParameterLocations = new ArrayList<>();
        parameterLocation = new HashMap<>();
    }

    public boolean raLocationWasSet() {
        return raLocation != null;
    }

    public void setRaLocation(StackLocation raLocation) {
        this.raLocation = raLocation;
    }

    public StackLocation getRaLocation() {
        return raLocation;
    }

    public void addFormalParameterLocation(StackLocation stackLocation) {
        formalParameterLocations.add(stackLocation);
    }

    public Map<Function, ArrayList<StackLocation>> getParameterLocation() {
        return parameterLocation;
    }
}
