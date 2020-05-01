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

    int size;

    private final Map<VirtualRegister, StackLocation> spillLocations;
    private final ArrayList<StackLocation> formalParameterLocations; // Fetch from caller's stack frame.
    private final Map<Function, ArrayList<StackLocation>> parameterLocation;

    public StackFrame(Function function) {
        this.function = function;
        size = 0;

        spillLocations = new LinkedHashMap<>();
        formalParameterLocations = new ArrayList<>();
        parameterLocation = new HashMap<>();
    }

    public int getSize() {
        return size;
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

    public void computeFrameSize() {
        int maxSpilledActualParameter = 0;
        int spilledVRCnt = spillLocations.size();
        for (ArrayList<StackLocation> parameters : parameterLocation.values())
            maxSpilledActualParameter = Integer.max(maxSpilledActualParameter, parameters.size() - 8);

        size = maxSpilledActualParameter + spilledVRCnt;

        for (int i = 0; i < formalParameterLocations.size(); i++) {
            StackLocation stackLocation = formalParameterLocations.get(i);
            stackLocation.setOffset((size + i) * 4);
        }
        int j = 0;
        for (StackLocation stackLocation : spillLocations.values()) {
            stackLocation.setOffset((j + maxSpilledActualParameter) * 4);
            j++;
        }
        for (ArrayList<StackLocation> parameters : parameterLocation.values()) {
            for (int k = 0; k < parameters.size(); k++) {
                StackLocation stackLocation = parameters.get(k);
                stackLocation.setOffset(k * 4);
            }
        }
    }
}
