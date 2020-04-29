package MxCompiler.RISCV.Operand.Register;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class PhysicalRegister extends Register {
    // ------ Static Member/Methods ------
    static public String[] prNames = {
            "zero", "ra", "sp", "gp", "tp",
            "t0", "t1", "t2", "s0", "s1",
            "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7",
            "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11",
            "t3", "t4", "t5", "t6"};
    static public String[] callerSavePRNames = {
            "ra", "t0", "t1", "t2",
            "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7",
            "t3", "t4", "t5", "t6"
    };
    static public String[] calleeSavePRNames = {
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11"
    };
    static public String[] allocatablePRNames = {
            // Except zero, sp, gp and tp.
            "ra", "t0", "t1", "t2", "s0", "s1",
            "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7",
            "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11",
            "t3", "t4", "t5", "t6"
    };

    static public Map<String, PhysicalRegister> prs;
    static public Map<String, PhysicalRegister> callerSavePRs;
    static public Map<String, PhysicalRegister> calleeSavePRs;
    static public Map<String, PhysicalRegister> allocatablePRs;

    static public Map<String, VirtualRegister> vrs;
    static public VirtualRegister zeroVR;
    static public VirtualRegister raVR;
    static public ArrayList<VirtualRegister> argVR;
    static public ArrayList<VirtualRegister> calleeSaveVRs;

    static {
        prs = new LinkedHashMap<>();
        callerSavePRs = new LinkedHashMap<>();
        calleeSavePRs = new LinkedHashMap<>();
        allocatablePRs = new LinkedHashMap<>();
        for (String name : prNames)
            prs.put(name, new PhysicalRegister(name));

        for (String name : callerSavePRNames)
            callerSavePRs.put(name, prs.get(name));
        for (String name : calleeSavePRNames)
            calleeSavePRs.put(name, prs.get(name));
        for (String name : allocatablePRNames)
            allocatablePRs.put(name, prs.get(name));

        vrs = new LinkedHashMap<>();
        for (String name : prNames) {
            VirtualRegister vr = new VirtualRegister("." + name);
            vr.fixColor(prs.get(name));
            vrs.put(name, vr);
        }

        zeroVR = vrs.get("zero");
        raVR = vrs.get("ra");
        argVR = new ArrayList<>();
        for (int i = 0; i < 8; i++)
            argVR.add(vrs.get("a" + i));
        calleeSaveVRs = new ArrayList<>();
        for (String name : calleeSavePRNames)
            calleeSaveVRs.add(vrs.get(name));
    }

    // ------ END ------

    private String name;

    public PhysicalRegister(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
