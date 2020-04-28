package MxCompiler.Backend;

import MxCompiler.IR.BasicBlock;
import MxCompiler.IR.Function;
import MxCompiler.IR.IRVisitor;
import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Instruction.CallInst;
import MxCompiler.IR.Instruction.LoadInst;
import MxCompiler.IR.Instruction.MoveInst;
import MxCompiler.IR.Instruction.ReturnInst;
import MxCompiler.IR.Instruction.StoreInst;
import MxCompiler.IR.Module;
import MxCompiler.IR.Operand.*;
import MxCompiler.IR.TypeSystem.*;
import MxCompiler.RISCV.Instruction.*;
import MxCompiler.RISCV.Instruction.BinaryInst.ITypeBinary;
import MxCompiler.RISCV.Instruction.BinaryInst.RTypeBinary;
import MxCompiler.RISCV.Instruction.Branch.BinaryBranch;
import MxCompiler.RISCV.Instruction.Branch.UnaryBranch;
import MxCompiler.RISCV.Operand.ASMOperand;
import MxCompiler.RISCV.Operand.Address.BaseOffsetAddr;
import MxCompiler.RISCV.Operand.Address.StackLocation;
import MxCompiler.RISCV.Operand.Immediate.Immediate;
import MxCompiler.RISCV.Operand.Immediate.IntImmediate;
import MxCompiler.RISCV.Operand.Register.PhysicalRegister;
import MxCompiler.RISCV.Operand.Register.VirtualRegister;
import MxCompiler.RISCV.Operand.Immediate.RelocationImmediate;
import MxCompiler.RISCV.StackFrame;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static MxCompiler.RISCV.Instruction.BinaryInst.ITypeBinary.OpName.*;
import static MxCompiler.RISCV.Instruction.BinaryInst.RTypeBinary.OpName.*;
import static MxCompiler.RISCV.Instruction.Branch.BinaryBranch.OpName.*;
import static MxCompiler.RISCV.Instruction.Branch.UnaryBranch.OpName.*;
import static MxCompiler.RISCV.Instruction.UnaryInst.OpName.*;

public class InstructionSelector implements IRVisitor {
    private MxCompiler.RISCV.Module ASMModule;

    private MxCompiler.RISCV.Function currentFunction;
    private MxCompiler.RISCV.BasicBlock currentBlock;

    public InstructionSelector() {
        ASMModule = new MxCompiler.RISCV.Module();
        currentFunction = null;
        currentBlock = null;
    }

    public MxCompiler.RISCV.Module getASMModule() {
        return ASMModule;
    }

    @Override
    public void visit(Module module) {
        for (GlobalVariable IRGlobalVariable : module.getGlobalVariableMap().values()) {
            String name = IRGlobalVariable.getName();
            MxCompiler.RISCV.Operand.GlobalVariable gv = new MxCompiler.RISCV.Operand.GlobalVariable(name);
            ASMModule.getGlobalVariableMap().put(name, gv);

            Operand init = IRGlobalVariable.getInit();
            assert init != null;

            if (IRGlobalVariable.getType() instanceof ArrayType) {
                assert IRGlobalVariable.getInit() instanceof ConstString;
                gv.setString(((ConstString) init).getValue());
            } else if (IRGlobalVariable.getType() instanceof IntegerType
                    && ((IntegerType) IRGlobalVariable.getType()).getBitWidth() == IntegerType.BitWidth.int1) {
                assert init instanceof ConstBool;
                gv.setBool(((ConstBool) init).getValue() ? 1 : 0);
            } else if (IRGlobalVariable.getType() instanceof IntegerType
                    && ((IntegerType) IRGlobalVariable.getType()).getBitWidth() == IntegerType.BitWidth.int32) {
                assert init instanceof ConstInt;
                gv.setInt(((int) ((ConstInt) init).getValue()));
            } else if (IRGlobalVariable.getType() instanceof PointerType) {
                assert init instanceof ConstNull;
                gv.setInt(0);
            }
        }
        for (Function IRExternalFunction : module.getExternalFunctionMap().values()) {
            String name = IRExternalFunction.getName();
            ASMModule.getExternalFunctionMap().put(name,
                    new MxCompiler.RISCV.Function(ASMModule, name, null));
        }
        for (Function IRFunction : module.getFunctionMap().values()) {
            String functionName = IRFunction.getName();
            ASMModule.getFunctionMap().put(functionName,
                    new MxCompiler.RISCV.Function(ASMModule, functionName, IRFunction));
        }

        for (Function IRFunction : module.getFunctionMap().values())
            IRFunction.accept(this);
    }

    @Override
    public void visit(Function function) {
        String functionName = function.getName();
        currentFunction = ASMModule.getFunctionMap().get(functionName);
        currentBlock = currentFunction.getEntranceBlock();

        // ------ Stack Frame ------
        StackFrame stackFrame = new StackFrame(currentFunction);
        currentFunction.setStackFrame(stackFrame);

        // ------ Save return address ------
        VirtualRegister savedRA = new VirtualRegister(PhysicalRegister.raVR.getName() + ".save");
        currentFunction.getSymbolTable().putASM(savedRA.getName(), savedRA);
        currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.MoveInst(currentBlock,
                savedRA, PhysicalRegister.raVR));

        // ------ Save callee-save registers ------
        for (VirtualRegister vr : PhysicalRegister.calleeSaveVRs) {
            VirtualRegister savedVR = new VirtualRegister(vr.getName() + ".save");
            currentFunction.getSymbolTable().putASM(savedVR.getName(), savedVR);
            currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.MoveInst(currentBlock, savedVR, vr));
        }

        // ------ Parameters ------
        ArrayList<Parameter> IRParameters = function.getParameters();
        // Fix the color of the first 8 parameters.
        for (int i = 0; i < Integer.min(IRParameters.size(), 8); i++) {
            Parameter parameter = IRParameters.get(i);
            VirtualRegister vr = currentFunction.getSymbolTable().getVR(parameter.getName());
            currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.MoveInst(currentBlock,
                    vr, PhysicalRegister.argVR.get(i)));
        }
        // Load spilled parameters from the frame of the caller.
        for (int i = 8; i < IRParameters.size(); i++) {
            Parameter parameter = IRParameters.get(i);
            VirtualRegister vr = currentFunction.getSymbolTable().getVR(parameter.getName());
            StackLocation stackLocation = new StackLocation(parameter.getName() + ".para");
            stackFrame.addFormalParameterLocation(stackLocation);
            currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.LoadInst(currentBlock, vr,
                    MxCompiler.RISCV.Instruction.LoadInst.ByteSize.lw, stackLocation));
        }

        // ------ Blocks ------
        for (BasicBlock block : function.getBlocks())
            block.accept(this);
    }

    @Override
    public void visit(BasicBlock block) {
        currentBlock = currentFunction.getBlockMap().get(block.getName());
        IRInstruction ptr = block.getInstHead();
        while (ptr != null) {
            ptr.accept(this);
            ptr = ptr.getInstNext();
        }
    }

    @Override
    public void visit(ReturnInst inst) {
        if (!(inst.getType() instanceof VoidType)) {
            VirtualRegister returnValue = getVROfOperand(inst.getReturnValue());
            currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.MoveInst(currentBlock,
                    PhysicalRegister.argVR.get(0), returnValue));
        }

        // ------ Recover saved callee-save registers ------
        for (VirtualRegister vr : PhysicalRegister.calleeSaveVRs) {
            VirtualRegister savedVR = currentFunction.getSymbolTable().getVR(vr.getName() + ".save");
            currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.MoveInst(currentBlock, vr, savedVR));
        }

        VirtualRegister savedRA = currentFunction.getSymbolTable().getVR(
                PhysicalRegister.raVR.getName() + ".save");
        currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.MoveInst(currentBlock,
                PhysicalRegister.raVR, savedRA));

        currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.ReturnInst(currentBlock));
    }

    @Override
    public void visit(BranchInst inst) {
        if (inst.isConditional()) {
            Operand cond = inst.getCond();
            MxCompiler.RISCV.BasicBlock thenBlock = currentFunction.getBlockMap().get(inst.getThenBlock().getName());
            MxCompiler.RISCV.BasicBlock elseBlock = currentFunction.getBlockMap().get(inst.getElseBlock().getName());

            if (cond instanceof Register
                    && ((Register) cond).getDef() instanceof IcmpInst
                    && cond.onlyHaveOneBranchUse()) {
                IcmpInst icmp = ((IcmpInst) ((Register) cond).getDef());
                if (icmp.shouldSwap(true))
                    icmp.swapOps();

                IRType type = icmp.getIrType();
                IcmpInst.IcmpName op = icmp.getOperator();
                Operand op1 = icmp.getOp1();
                Operand op2 = icmp.getOp2();
                VirtualRegister rs1 = currentFunction.getSymbolTable().getVR(op1.getName());
                VirtualRegister rs2;
                if (type instanceof IntegerType) {
                    if (op2 instanceof Constant) {
                        long value = op2 instanceof ConstBool
                                ? (((ConstBool) op2).getValue() ? 1 : 0) : ((ConstInt) op2).getValue();

                        if (value != 0) {
                            rs2 = new VirtualRegister("loadImmediate");
                            currentFunction.getSymbolTable().putASMRename(rs2.getName(), rs2);
                            if (needToLoadImmediate(value)) {
                                currentBlock.addInstruction(new LoadImmediate(currentBlock,
                                        rs2, new IntImmediate(value)));
                            } else {
                                currentBlock.addInstruction(new ITypeBinary(currentBlock, addi, PhysicalRegister.zeroVR,
                                        new IntImmediate(value), rs2));
                            }
                        } else
                            rs2 = PhysicalRegister.zeroVR;
                    } else
                        rs2 = currentFunction.getSymbolTable().getVR(op2.getName());
                } else if (type instanceof PointerType) {
                    if (op2 instanceof Constant) {
                        assert op2 instanceof ConstNull;
                        rs2 = PhysicalRegister.zeroVR;
                    } else
                        rs2 = currentFunction.getSymbolTable().getVR(op2.getName());
                } else
                    throw new RuntimeException();

                BinaryBranch.OpName branchOp = op == IcmpInst.IcmpName.eq ? bne
                        : op == IcmpInst.IcmpName.ne ? beq
                        : op == IcmpInst.IcmpName.sgt ? ble
                        : op == IcmpInst.IcmpName.sge ? blt
                        : op == IcmpInst.IcmpName.slt ? bge
                        : bgt;
                currentBlock.addInstruction(new BinaryBranch(currentBlock, branchOp, rs1, rs2, elseBlock));
                currentBlock.addInstruction(new JumpInst(currentBlock, thenBlock));
                return;
            }

            VirtualRegister condVR = currentFunction.getSymbolTable().getVR(cond.getName());
            currentBlock.addInstruction(new UnaryBranch(currentBlock, beqz, condVR, elseBlock));
            currentBlock.addInstruction(new JumpInst(currentBlock, thenBlock));
        } else {
            MxCompiler.RISCV.BasicBlock thenBlock = currentFunction.getBlockMap().get(inst.getThenBlock().getName());
            currentBlock.addInstruction(new JumpInst(currentBlock, thenBlock));
        }
    }

    @Override
    public void visit(BinaryOpInst inst) {
        if (inst.shouldSwapOperands())
            inst.swapOperands();

        Operand lhs = inst.getLhs();
        Operand rhs = inst.getRhs();
        VirtualRegister lhsOperand;
        ASMOperand rhsOperand;
        VirtualRegister result = currentFunction.getSymbolTable().getVR(inst.getResult().getName());

        Object opName;
        BinaryOpInst.BinaryOpName instOp = inst.getOp();
        switch (instOp) {
            case add: case and: case or: case xor:
                lhsOperand = getVROfOperand(lhs);
                rhsOperand = getOperand(rhs);
                if (rhsOperand instanceof Immediate) {
                    opName = instOp == BinaryOpInst.BinaryOpName.add ? addi
                            : instOp == BinaryOpInst.BinaryOpName.and ? andi
                            : instOp == BinaryOpInst.BinaryOpName.or ? ori
                            : xori;
                    currentBlock.addInstruction(new ITypeBinary(currentBlock, ((ITypeBinary.OpName) opName),
                            lhsOperand, ((Immediate) rhsOperand), result));
                } else {
                    opName = instOp == BinaryOpInst.BinaryOpName.add ? add
                            : instOp == BinaryOpInst.BinaryOpName.and ? and
                            : instOp == BinaryOpInst.BinaryOpName.or ? or
                            : xor;
                    currentBlock.addInstruction(new RTypeBinary(currentBlock, ((RTypeBinary.OpName) opName),
                            lhsOperand, ((VirtualRegister) rhsOperand), result));
                }
                break;
            case sub:
                lhsOperand = getVROfOperand(lhs);
                rhsOperand = getOperand(rhs);
                if (rhsOperand instanceof Immediate) {
                    assert rhsOperand instanceof IntImmediate;
                    ((IntImmediate) rhsOperand).minusImmediate();
                    currentBlock.addInstruction(new ITypeBinary(currentBlock, ITypeBinary.OpName.addi,
                            lhsOperand, ((Immediate) rhsOperand), result));
                } else {
                    currentBlock.addInstruction(new RTypeBinary(currentBlock, RTypeBinary.OpName.sub,
                            lhsOperand, ((VirtualRegister) rhsOperand), result));
                }
                break;
            case mul: case sdiv: case srem:
                opName = instOp == BinaryOpInst.BinaryOpName.mul ? RTypeBinary.OpName.mul
                        : instOp == BinaryOpInst.BinaryOpName.sdiv ? RTypeBinary.OpName.div
                        : RTypeBinary.OpName.rem;
                lhsOperand = getVROfOperand(lhs);
                rhsOperand = getVROfOperand(rhs);
                currentBlock.addInstruction(new RTypeBinary(currentBlock, ((RTypeBinary.OpName) opName),
                        lhsOperand, ((VirtualRegister) rhsOperand), result));
                break;
            case shl: case ashr:
                if (rhs instanceof ConstInt && (((ConstInt) rhs).getValue() >= 32 || ((ConstInt) rhs).getValue() <= 0))
                    break;
                lhsOperand = getVROfOperand(lhs);
                rhsOperand = getOperand(rhs);
                if (rhsOperand instanceof Immediate) {
                    opName = instOp == BinaryOpInst.BinaryOpName.shl ? slli : srai;
                    currentBlock.addInstruction(new ITypeBinary(currentBlock, ((ITypeBinary.OpName) opName),
                            lhsOperand, ((Immediate) rhsOperand), result));
                } else {
                    opName = instOp == BinaryOpInst.BinaryOpName.shl ? sll : sra;
                    currentBlock.addInstruction(new RTypeBinary(currentBlock, ((RTypeBinary.OpName) opName),
                            lhsOperand, ((VirtualRegister) rhsOperand), result));
                }
                break;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public void visit(AllocateInst inst) {
        // Do nothing.
    }

    @Override
    public void visit(LoadInst inst) {
        VirtualRegister rd = currentFunction.getSymbolTable().getVR(inst.getResult().getName());
        assert inst.getType() instanceof IntegerType || inst.getType() instanceof PointerType;
        MxCompiler.RISCV.Instruction.LoadInst.ByteSize size = inst.getType().getBytes() == 1
                ? MxCompiler.RISCV.Instruction.LoadInst.ByteSize.lb
                : MxCompiler.RISCV.Instruction.LoadInst.ByteSize.lw;

        if (inst.getPointer() instanceof GlobalVariable) {
            MxCompiler.RISCV.Operand.GlobalVariable gv =
                    ASMModule.getGlobalVariableMap().get(inst.getPointer().getName());
            VirtualRegister lui = new VirtualRegister("luiHigh");
            currentFunction.getSymbolTable().putASMRename(lui.getName(), lui);
            currentBlock.addInstruction(new LoadUpperImmediate(currentBlock, lui,
                    new RelocationImmediate(RelocationImmediate.Type.high, gv)));
            currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.LoadInst(currentBlock, rd, size,
                    new BaseOffsetAddr(lui, new RelocationImmediate(RelocationImmediate.Type.low, gv))));
        } else {
            if (inst.getPointer() instanceof ConstNull) {
                currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.LoadInst(currentBlock, rd, size,
                        new BaseOffsetAddr(PhysicalRegister.zeroVR, new IntImmediate(0))));
            } else {
                assert inst.getPointer() instanceof Parameter || inst.getPointer() instanceof Register;
                VirtualRegister pointer = currentFunction.getSymbolTable().getVR(inst.getPointer().getName());
                if (currentFunction.getGepAddrMap().containsKey(pointer)) {
                    BaseOffsetAddr addr = currentFunction.getGepAddrMap().get(pointer);
                    currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.LoadInst(currentBlock,
                            rd, size, addr));
                } else {
                    currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.LoadInst(currentBlock,
                            rd, size, new BaseOffsetAddr(pointer, new IntImmediate(0))));
                }
            }
        }
    }

    @Override
    public void visit(StoreInst inst) {
        VirtualRegister value = getVROfOperand(inst.getValue());
        IRType irType = inst.getValue().getType();
        assert irType instanceof IntegerType || irType instanceof PointerType;
        MxCompiler.RISCV.Instruction.StoreInst.ByteSize size = irType.getBytes() == 1
                ? MxCompiler.RISCV.Instruction.StoreInst.ByteSize.sb
                : MxCompiler.RISCV.Instruction.StoreInst.ByteSize.sw;

        if (inst.getPointer() instanceof GlobalVariable) {
            MxCompiler.RISCV.Operand.GlobalVariable gv =
                    ASMModule.getGlobalVariableMap().get(inst.getPointer().getName());
            VirtualRegister lui = new VirtualRegister("luiHigh");
            currentFunction.getSymbolTable().putASMRename(lui.getName(), lui);
            currentBlock.addInstruction(new LoadUpperImmediate(currentBlock, lui,
                    new RelocationImmediate(RelocationImmediate.Type.high, gv)));
            currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.StoreInst(currentBlock, value, size,
                    new BaseOffsetAddr(lui, new RelocationImmediate(RelocationImmediate.Type.low, gv))));
        } else {
            if (inst.getPointer() instanceof ConstNull) {
                currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.StoreInst(currentBlock, value, size,
                        new BaseOffsetAddr(PhysicalRegister.zeroVR, new IntImmediate(0))));
            } else {
                assert inst.getPointer() instanceof Parameter || inst.getPointer() instanceof Register;
                VirtualRegister pointer = currentFunction.getSymbolTable().getVR(inst.getPointer().getName());
                if (currentFunction.getGepAddrMap().containsKey(pointer)) {
                    BaseOffsetAddr addr = currentFunction.getGepAddrMap().get(pointer);
                    currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.StoreInst(currentBlock,
                            value, size, addr));
                } else {
                    currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.StoreInst(currentBlock,
                            value, size, new BaseOffsetAddr(pointer, new IntImmediate(0))));
                }
            }
        }
    }

    @Override
    public void visit(GetElementPtrInst inst) {
        VirtualRegister rd = currentFunction.getSymbolTable().getVR(inst.getResult().getName());

        if (inst.getPointer() instanceof GlobalVariable) { // gep string
            currentBlock.addInstruction(new LoadAddressInst(currentBlock, rd,
                    ASMModule.getGlobalVariableMap().get(inst.getPointer().getName())));
        } else if (inst.getIndex().size() == 1) { // gep array
            VirtualRegister pointer = currentFunction.getSymbolTable().getVR(inst.getPointer().getName());
            Operand index = inst.getIndex().get(0);
            if (index instanceof Constant) {
                assert index instanceof ConstInt;
                long value = ((ConstInt) index).getValue() * 4; // 4 is the size of a pointer.
                currentFunction.getGepAddrMap().put(rd, new BaseOffsetAddr(pointer, new IntImmediate(value)));
            } else {
                VirtualRegister rs1 = currentFunction.getSymbolTable().getVR(index.getName());
                VirtualRegister rs2 = new VirtualRegister("slli");
                currentFunction.getSymbolTable().putASMRename(rs2.getName(), rs2);
                currentBlock.addInstruction(new ITypeBinary(currentBlock, slli, rs1, new IntImmediate(2), rs2));
                currentBlock.addInstruction(new RTypeBinary(currentBlock, add, pointer, rs2, rd));
            }
        } else { // gep class
            if (inst.getPointer() instanceof ConstNull) {
                currentBlock.addInstruction(new ITypeBinary(currentBlock, addi, PhysicalRegister.zeroVR,
                        new IntImmediate(((int) ((ConstInt) inst.getIndex().get(1)).getValue())), rd));
            } else {
                assert inst.getPointer().getType() instanceof PointerType
                        && ((PointerType) inst.getPointer().getType()).getBaseType() instanceof StructureType;
                assert inst.getIndex().size() == 2;
                assert inst.getIndex().get(0) instanceof ConstInt
                        && ((ConstInt) inst.getIndex().get(0)).getValue() == 0;
                assert inst.getIndex().get(1) instanceof ConstInt;
                VirtualRegister pointer = currentFunction.getSymbolTable().getVR(inst.getPointer().getName());
                StructureType structureType = ((StructureType) ((PointerType)
                        inst.getPointer().getType()).getBaseType());
                int index = ((int) ((ConstInt) inst.getIndex().get(1)).getValue());
                int offset = structureType.calcOffset(index);
                currentFunction.getGepAddrMap().put(rd, new BaseOffsetAddr(pointer, new IntImmediate(offset)));
            }
        }
    }

    @Override
    public void visit(BitCastToInst inst) {
        VirtualRegister src = currentFunction.getSymbolTable().getVR(inst.getSrc().getName());
        VirtualRegister dest = currentFunction.getSymbolTable().getVR(inst.getResult().getName());
        currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.MoveInst(currentBlock, dest, src));
    }

    @Override
    public void visit(IcmpInst inst) {
        if (inst.getResult().onlyHaveOneBranchUse()) {
            // Do nothing. Wait until dealing with BranchInst.
            return;
        }

        if (inst.shouldSwap(true))
            inst.swapOps();

        IRType type = inst.getIrType();
        Operand op1 = inst.getOp1();
        Operand op2 = inst.getOp2();
        VirtualRegister rd = currentFunction.getSymbolTable().getVR(inst.getResult().getName());
        if (type instanceof IntegerType) {
            VirtualRegister rs1 = currentFunction.getSymbolTable().getVR(op1.getName());
            if (op2 instanceof Constant) { // I-type
                inst.convertLeGeToLtGt();
                IcmpInst.IcmpName op = inst.getOperator();

                long value = op2 instanceof ConstBool
                        ? (((ConstBool) op2).getValue() ? 1 : 0) : ((ConstInt) op2).getValue();
                VirtualRegister rs2 = new VirtualRegister("loadImmediate");
                VirtualRegister rs3 = new VirtualRegister("xor");
                switch (op) {
                    case slt:
                        if (needToLoadImmediate(value)) {
                            currentFunction.getSymbolTable().putASMRename(rs2.getName(), rs2);
                            currentBlock.addInstruction(new LoadImmediate(currentBlock, rs2, new IntImmediate(value)));
                            currentBlock.addInstruction(new RTypeBinary(currentBlock, slt, rs1, rs2, rd));
                        } else if (value != 0) {
                            currentBlock.addInstruction(new ITypeBinary(currentBlock, slti, rs1,
                                    new IntImmediate(value), rd));
                        } else { // value == 0
                            currentBlock.addInstruction(new UnaryInst(currentBlock, sltz, rs1, rd));
                        }
                        break;
                    case sgt:
                        if (needToLoadImmediate(value)) {
                            currentFunction.getSymbolTable().putASMRename(rs2.getName(), rs2);
                            currentBlock.addInstruction(new LoadImmediate(currentBlock, rs2, new IntImmediate(value)));
                            currentBlock.addInstruction(new RTypeBinary(currentBlock, slt, rs2, rs1, rd));
                        } else if (value != 0) {
                            currentFunction.getSymbolTable().putASMRename(rs2.getName(), rs2);
                            currentBlock.addInstruction(new ITypeBinary(currentBlock, addi, PhysicalRegister.zeroVR,
                                    new IntImmediate(value), rs2));
                            currentBlock.addInstruction(new RTypeBinary(currentBlock, slt, rs2, rs1, rd));
                        } else { // value == 0
                            currentBlock.addInstruction(new UnaryInst(currentBlock, sgtz, rs1, rd));
                        }
                        break;
                    case eq: case ne:
                        UnaryInst.OpName opName = op == IcmpInst.IcmpName.eq ? seqz : snez;
                        if (needToLoadImmediate(value)) {
                            currentFunction.getSymbolTable().putASMRename(rs2.getName(), rs2);
                            currentFunction.getSymbolTable().putASMRename(rs3.getName(), rs3);

                            currentBlock.addInstruction(new LoadImmediate(currentBlock, rs2, new IntImmediate(value)));
                            currentBlock.addInstruction(new RTypeBinary(currentBlock, xor, rs1, rs2, rs3));
                            currentBlock.addInstruction(new UnaryInst(currentBlock, opName, rs3, rd));
                        } else if (value != 0) {
                            currentFunction.getSymbolTable().putASMRename(rs3.getName(), rs3);
                            currentBlock.addInstruction(new ITypeBinary(currentBlock, xori, rs1,
                                    new IntImmediate(value), rs3));
                            currentBlock.addInstruction(new UnaryInst(currentBlock, opName, rs3, rd));
                        } else { // value = 0
                            currentBlock.addInstruction(new UnaryInst(currentBlock, opName, rs1, rd));
                        }
                        break;
                    default:
                        System.out.println(op);
                        System.out.println(op1);
                        System.out.println(op2);
                        throw new RuntimeException();
                }
            } else {
                IcmpInst.IcmpName op = inst.getOperator();
                VirtualRegister rs2 = currentFunction.getSymbolTable().getVR(op2.getName());
                VirtualRegister rs3 = new VirtualRegister("slt");
                VirtualRegister rs4 = new VirtualRegister("xor");
                switch (op) {
                    case slt:
                        currentBlock.addInstruction(new RTypeBinary(currentBlock, slt, rs1, rs2, rd));
                        break;
                    case sgt:
                        currentBlock.addInstruction(new RTypeBinary(currentBlock, slt, rs2, rs1, rd));
                        break;
                    case sle:
                        currentFunction.getSymbolTable().putASMRename(rs3.getName(), rs3);
                        currentBlock.addInstruction(new RTypeBinary(currentBlock, slt, rs2, rs1, rs3));
                        currentBlock.addInstruction(new ITypeBinary(currentBlock, xori, rs3,
                                new IntImmediate(1), rd));
                        break;
                    case sge:
                        currentFunction.getSymbolTable().putASMRename(rs3.getName(), rs3);
                        currentBlock.addInstruction(new RTypeBinary(currentBlock, slt, rs1, rs2, rs3));
                        currentBlock.addInstruction(new ITypeBinary(currentBlock, xori, rs3,
                                new IntImmediate(1), rd));
                        break;
                    case eq:
                        currentFunction.getSymbolTable().putASMRename(rs4.getName(), rs4);
                        currentBlock.addInstruction(new RTypeBinary(currentBlock, xor, rs1, rs2, rs4));
                        currentBlock.addInstruction(new UnaryInst(currentBlock, seqz, rs4, rd));
                        break;
                    case ne:
                        currentFunction.getSymbolTable().putASMRename(rs4.getName(), rs4);
                        currentBlock.addInstruction(new RTypeBinary(currentBlock, xor, rs1, rs2, rs4));
                        currentBlock.addInstruction(new UnaryInst(currentBlock, snez, rs4, rd));
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
        } else if (type instanceof PointerType) {
            VirtualRegister rs1 = currentFunction.getSymbolTable().getVR(op1.getName());
            IcmpInst.IcmpName op = inst.getOperator();
            if (op2 instanceof Constant) {
                assert op2 instanceof ConstNull;
                switch (op) {
                    case eq:
                        currentBlock.addInstruction(new UnaryInst(currentBlock, seqz, rs1, rd));
                        break;
                    case ne:
                        currentBlock.addInstruction(new UnaryInst(currentBlock, snez, rs1, rd));
                        break;
                    default:
                        throw new RuntimeException();
                }
            } else {
                VirtualRegister rs2 = currentFunction.getSymbolTable().getVR(op2.getName());
                VirtualRegister rs3 = new VirtualRegister("xor");

                currentFunction.getSymbolTable().putASMRename(rs3.getName(), rs3);
                currentBlock.addInstruction(new RTypeBinary(currentBlock, xor, rs1, rs2, rs3));
                switch (op) {
                    case eq:
                        currentBlock.addInstruction(new UnaryInst(currentBlock, seqz, rs3, rd));
                        break;
                    case ne:
                        currentBlock.addInstruction(new UnaryInst(currentBlock, snez, rs3, rd));
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
        } else
            throw new RuntimeException();
    }

    @Override
    public void visit(PhiInst inst) {
        // Do nothing.
    }

    @Override
    public void visit(CallInst inst) {
        MxCompiler.RISCV.Function callee = ASMModule.getFunctionMap().get(inst.getFunction().getName());
        ArrayList<Operand> parameters = inst.getParameters();

        for (int i = 0; i < Integer.min(8, parameters.size()); i++) {
            VirtualRegister parameter = getVROfOperand(parameters.get(i));
            currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.MoveInst(currentBlock,
                    PhysicalRegister.argVR.get(i), parameter));
        }

        StackFrame stackFrame = currentFunction.getStackFrame();
        if (stackFrame.getParameterLocation().containsKey(callee)) {
            ArrayList<StackLocation> stackLocations = stackFrame.getParameterLocation().get(callee);
            for (int i = 8; i < parameters.size(); i++) {
                VirtualRegister parameter = getVROfOperand(parameters.get(i));
                currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.StoreInst(currentBlock, parameter,
                        MxCompiler.RISCV.Instruction.StoreInst.ByteSize.sw, stackLocations.get(i - 8)));
            }
        } else {
            ArrayList<StackLocation> stackLocations = new ArrayList<>();
            for (int i = 8; i < parameters.size(); i++) {
                VirtualRegister parameter = getVROfOperand(parameters.get(i));
                StackLocation stackLocation = new StackLocation(".para" + i);
                stackLocations.add(stackLocation);

                currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.StoreInst(currentBlock, parameter,
                        MxCompiler.RISCV.Instruction.StoreInst.ByteSize.sw, stackLocation));
            }
            stackFrame.getParameterLocation().put(callee, stackLocations);
        }

        MxCompiler.RISCV.Instruction.CallInst callInst = new MxCompiler.RISCV.Instruction.CallInst(currentBlock,
                callee);
        currentBlock.addInstruction(callInst);

        if (!inst.isVoidCall()) {
            VirtualRegister result = currentFunction.getSymbolTable().getVR(inst.getResult().getName());
            currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.MoveInst(currentBlock,
                    result, PhysicalRegister.argVR.get(0)));
        }
    }

    @Override
    public void visit(MoveInst inst) {
        VirtualRegister dest = currentFunction.getSymbolTable().getVR(inst.getResult().getName());
        if (inst.getSource() instanceof Constant) {
            ASMOperand src = getOperand(inst.getSource());
            if (src instanceof VirtualRegister) {
                currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.MoveInst(currentBlock,
                        dest, ((VirtualRegister) src)));
            } else {
                assert src instanceof Immediate;
                currentBlock.addInstruction(new ITypeBinary(currentBlock, addi,
                        PhysicalRegister.zeroVR, ((Immediate) src), dest));
            }
        } else {
            VirtualRegister src = currentFunction.getSymbolTable().getVR(inst.getSource().getName());
            currentBlock.addInstruction(new MxCompiler.RISCV.Instruction.MoveInst(currentBlock, dest, src));
        }
    }

    static private boolean needToLoadImmediate(long value) {
        return value >= (1 << 11) || value < -(1 << 11);
    }

    private VirtualRegister getVROfOperand(Operand operand) {
        if (operand instanceof ConstBool) {
            if (((ConstBool) operand).getValue()) {
                VirtualRegister constBool = new VirtualRegister("constBool");
                currentFunction.getSymbolTable().putASMRename(constBool.getName(), constBool);
                currentBlock.addInstruction(new ITypeBinary(currentBlock, ITypeBinary.OpName.addi,
                        PhysicalRegister.zeroVR, new IntImmediate(1), constBool));
                return constBool;
            } else
                return PhysicalRegister.zeroVR;
        } else if (operand instanceof ConstInt) {
            long value = ((ConstInt) operand).getValue();
            if (value == 0)
                return PhysicalRegister.zeroVR;
            else {
                VirtualRegister constInt = new VirtualRegister("constInt");
                currentFunction.getSymbolTable().putASMRename(constInt.getName(), constInt);
                if (needToLoadImmediate(value)) {
                    currentBlock.addInstruction(new LoadImmediate(currentBlock, constInt, new IntImmediate(value)));
                } else {
                    currentBlock.addInstruction(new ITypeBinary(currentBlock, ITypeBinary.OpName.addi,
                            PhysicalRegister.zeroVR, new IntImmediate(value), constInt));
                }
                return constInt;
            }
        } else if (operand instanceof ConstNull) {
            return PhysicalRegister.zeroVR;
        } else if (operand instanceof GlobalVariable) {
            throw new RuntimeException();
        } else if (operand instanceof Parameter) {
            return currentFunction.getSymbolTable().getVR(operand.getName());
        } else if (operand instanceof Register) {
            return currentFunction.getSymbolTable().getVR(operand.getName());
        } else
            throw new RuntimeException();
    }

    private ASMOperand getOperand(Operand operand) {
        if (operand instanceof ConstBool) {
            boolean value = ((ConstBool) operand).getValue();
            return new IntImmediate(value ? 1 : 0);
        } else if (operand instanceof ConstInt) {
            long value = ((ConstInt) operand).getValue();
            if (needToLoadImmediate(value))
                return getVROfOperand(operand);
            else
                return new IntImmediate(value);
        } else if (operand instanceof ConstNull) {
            return PhysicalRegister.zeroVR;
        } else if (operand instanceof GlobalVariable) {
            throw new RuntimeException();
        } else if (operand instanceof Register || operand instanceof Parameter) {
            return getVROfOperand(operand);
        } else
            throw new RuntimeException();
    }
}
