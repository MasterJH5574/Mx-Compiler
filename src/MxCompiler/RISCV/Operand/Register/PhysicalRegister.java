package MxCompiler.RISCV.Operand.Register;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class PhysicalRegister extends Register {
    // ------ Static Member/Methods ------
    static private String[] prNames = {
            "zero", "ra", "sp", "gp", "tp",
            "t0", "t1", "t2", "s0", "s1",
            "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7",
            "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11",
            "t3", "t4", "t5", "t6"};
    static private String[] callerSavePRNames = {
            "ra", "t0", "t1", "t2",
            "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7",
            "t3", "t4", "t5", "t6"
    };
    static private String[] calleeSavePRNames = {
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7", "s8", "s9", "s10", "s11"
    };
    static private String[] allocatablePRNames = {
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
    static public VirtualRegister zeroVR;
    static public VirtualRegister raVR;
    static public ArrayList<VirtualRegister> argVR;

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


        zeroVR = new VirtualRegister(".zero");
        zeroVR.fixColor(prs.get("zero"));
        raVR = new VirtualRegister(".ra");
        raVR.fixColor(prs.get("ra"));
        argVR = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            VirtualRegister arg = new VirtualRegister(".a" + i);
            arg.fixColor(prs.get("a" + i));
            argVR.add(arg);
        }
    }

    // ------ END ------

    private String name;

    public PhysicalRegister(String name) {
        this.name = name;
    }
}
