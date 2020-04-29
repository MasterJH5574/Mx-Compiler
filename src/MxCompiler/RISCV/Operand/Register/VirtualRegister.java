package MxCompiler.RISCV.Operand.Register;

import MxCompiler.RISCV.Instruction.ASMInstruction;
import MxCompiler.RISCV.Instruction.LoadInst;
import MxCompiler.RISCV.Instruction.MoveInst;
import MxCompiler.RISCV.Instruction.StoreInst;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class VirtualRegister extends Register {
    private String name;

    // Member used for Register Allocator.
    private ArrayList<VirtualRegister> adjList;
    private int degree;
    private Set<MoveInst> moveList;
    private VirtualRegister alias;
    private boolean colorFixed;
    private PhysicalRegister colorPR;
    private double spillCost;

    public VirtualRegister(String name) {
        this.name = name;

        adjList = new ArrayList<>();
        degree = 0;
        moveList = new HashSet<>();
        alias = null;
        colorFixed = false;
        colorPR = null;
        spillCost = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void fixColor(PhysicalRegister pr) {
        colorFixed = true;
        colorPR = pr;
    }

    public void clearColoringData() {
        assert !colorFixed;

        adjList = new ArrayList<>();
        degree = 0;
        moveList = new HashSet<>();
        alias = null;
        colorPR = null;
        spillCost = 0;
    }

    public ArrayList<VirtualRegister> getAdjList() {
        return adjList;
    }

    public int getDegree() {
        return degree;
    }

    public void increaseDegree() {
        degree++;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public Set<MoveInst> getMoveList() {
        return moveList;
    }

    public VirtualRegister getAlias() {
        return alias;
    }

    public void setAlias(VirtualRegister alias) {
        this.alias = alias;
    }

    public PhysicalRegister getColorPR() {
        return colorPR;
    }

    public boolean hasAColor() {
        return colorPR != null;
    }

    public void setColorPR(PhysicalRegister colorPR) {
        this.colorPR = colorPR;
    }

    public void increaseSpillCost(double cost) {
        spillCost += cost;
    }

    private boolean haveNegativeSpillCosts() {
        if (getDef().size() == 1 && getUse().size() == 1) {
            ASMInstruction def = getDef().keySet().iterator().next();
            ASMInstruction use = getUse().keySet().iterator().next();
            if (def instanceof LoadInst && use instanceof StoreInst)
                return ((LoadInst) def).getAddr().equals(((StoreInst) use).getAddr());
        }
        return false;
    }

    private boolean haveInfiniteSpillCosts() {
        return getDef().size() == 1 && getUse().size() == 1
                && getDef().keySet().iterator().next().getNextInst() == getUse().keySet().iterator().next();
    }

    public double computeSpillRatio() {
        if (haveNegativeSpillCosts())
            return Double.NEGATIVE_INFINITY;
        else if (haveInfiniteSpillCosts())
            return Double.POSITIVE_INFINITY;
        else
            return spillCost / degree;
    }
}
