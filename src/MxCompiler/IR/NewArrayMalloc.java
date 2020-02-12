package MxCompiler.IR;

import MxCompiler.IR.Instruction.*;
import MxCompiler.IR.Operand.ConstInt;
import MxCompiler.IR.Operand.Operand;
import MxCompiler.IR.Operand.Register;
import MxCompiler.IR.TypeSystem.IRType;
import MxCompiler.IR.TypeSystem.IntegerType;
import MxCompiler.IR.TypeSystem.PointerType;

import java.util.ArrayList;

public class NewArrayMalloc {
    static public Operand generate(int cur, ArrayList<Operand> sizeList, IRType irType,
                                   Module module, IRBuilder irBuilder) {
        BasicBlock currentBlock = irBuilder.getCurrentBlock();
        Function currentFunction = irBuilder.getCurrentFunction();
        Function function = module.getExternalFunctionMap().get("malloc");
        ArrayList<Operand> parameters = new ArrayList<>();
        Register mallocResult = new Register(new PointerType(new IntegerType(IntegerType.BitWidth.int8)),
                "malloc");
        currentFunction.getSymbolTable().put(mallocResult.getName(), mallocResult);

        // Calculate size
        int baseSize = irType.getBytes();
        Register bytesMul = new Register(new IntegerType(IntegerType.BitWidth.int32), "bytesMul");
        Register bytes = new Register(new IntegerType(IntegerType.BitWidth.int32), "bytes");
        currentFunction.getSymbolTable().put(bytesMul.getName(), bytesMul);
        currentFunction.getSymbolTable().put(bytes.getName(), bytes);
        currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.mul,
                sizeList.get(cur), new ConstInt(baseSize), bytesMul));
        currentBlock.addInstruction(new BinaryOpInst(currentBlock, BinaryOpInst.BinaryOpName.add,
                bytesMul, new ConstInt(4), bytes));
        parameters.add(bytes);

        // Call malloc
        currentBlock.addInstruction(new CallInst(currentBlock, function, parameters, mallocResult));
        // Cast to int32
        Register mallocInt32 = new Register(new PointerType(new IntegerType(IntegerType.BitWidth.int32)),
                "mallocInt32");
        currentFunction.getSymbolTable().put(mallocInt32.getName(), mallocInt32);
        currentBlock.addInstruction(new BitCastToInst(currentBlock, mallocResult,
                new PointerType(new IntegerType(IntegerType.BitWidth.int32)), mallocInt32));
        // Store size
        currentBlock.addInstruction(new StoreInst(currentBlock, sizeList.get(cur), mallocInt32));
        // GetElementPtr to next
        Register arrayHeadInt32 = new Register(new PointerType(new IntegerType(IntegerType.BitWidth.int32)),
                "arrayHeadInt32");
        currentFunction.getSymbolTable().put(arrayHeadInt32.getName(), arrayHeadInt32);
        ArrayList<Operand> index = new ArrayList<>();
        index.add(new ConstInt(1));
        currentBlock.addInstruction(new GetElementPtrInst(currentBlock, mallocInt32, index, arrayHeadInt32));
        // Cast to object type
        Register arrayHead = new Register(irType, "arrayHead");
        currentFunction.getSymbolTable().put(arrayHead.getName(), arrayHead);
        currentBlock.addInstruction(new BitCastToInst(currentBlock, arrayHeadInt32, irType, arrayHead));

        // Generate for the next dimension
        if (cur != sizeList.size() - 1) {
            // GetElementPtr to tail
            Register arrayTail = new Register(new PointerType(irType), "arrayTail");
            currentFunction.getSymbolTable().put(arrayTail.getName(), arrayTail);
            index = new ArrayList<>();
            index.add(sizeList.get(cur));
            currentBlock.addInstruction(new GetElementPtrInst(currentBlock, arrayHead, index, arrayTail));
            // Allocate temporary variable arrayPointer
            Register arrayPtrAddr = new Register(new PointerType(irType), "arrayPtrAddr");
            currentBlock.addInstructionAtFront(new AllocateInst(currentBlock, arrayPtrAddr, irType));
            // Store arrayHead to arrayPtrAddr
            currentBlock.addInstruction(new StoreInst(currentBlock, arrayHead, arrayPtrAddr));

            // ------- LOOP ------
            // Initialize blocks
            BasicBlock loopCond = new BasicBlock(currentFunction, "newLoopCond");
            BasicBlock loopBody = new BasicBlock(currentFunction, "newLoopBody");
            BasicBlock loopMerge = new BasicBlock(currentFunction, "newLoopMerge");
            currentFunction.getSymbolTable().put(loopCond.getName(), loopCond);
            currentFunction.getSymbolTable().put(loopBody.getName(), loopBody);
            currentFunction.getSymbolTable().put(loopMerge.getName(), loopMerge);
            // Jump to loopCond
            currentBlock.addInstruction(new BranchInst(currentBlock, null, loopCond, null));
            currentBlock = loopCond;
            irBuilder.setCurrentBlock(loopCond);
            // Check condition: arrayPointer != arrayHead
            Register arrayPointer = new Register(irType, "arrayPointer");
            Register cmpResult = new Register(new IntegerType(IntegerType.BitWidth.int1), "ptrCmpResult");
            currentFunction.getSymbolTable().put(arrayPointer.getName(), arrayPointer);
            currentFunction.getSymbolTable().put(cmpResult.getName(), cmpResult);
            currentBlock.addInstruction(new LoadInst(currentBlock, irType, arrayPtrAddr, arrayPointer));
            currentBlock.addInstruction(new IcmpInst(currentBlock, IcmpInst.IcmpName.slt,
                    irType, arrayPointer, arrayTail, cmpResult));
            // cond == true: Jump to loopBody
            // cond == false: Jump to loopMerge
            currentBlock.addInstruction(new BranchInst(currentBlock, cmpResult, loopBody, loopMerge));
            // loopBody: Call NewArrayMalloc.generate recursively
            //           Add arrayPointer
            currentBlock = loopBody;
            irBuilder.setCurrentBlock(currentBlock);
            Operand arrayHeadNextDim = generate(cur + 1, sizeList, irType, module, irBuilder);
            currentBlock.addInstruction(new StoreInst(currentBlock, arrayHeadNextDim, arrayPointer));
            // GetElementPtr to next arrayPointer
            Register nextArrayPtr = new Register(irType, "nextArrayPtr");
            currentFunction.getSymbolTable().put(nextArrayPtr.getName(), nextArrayPtr);
            index = new ArrayList<>();
            index.add(new ConstInt(1));
            currentBlock.addInstruction(new GetElementPtrInst(currentBlock, arrayPointer, index, nextArrayPtr));
            // Store nextArrayPtr to arrayPtrAddr
            currentBlock.addInstruction(new StoreInst(currentBlock, nextArrayPtr, arrayPtrAddr));
            // Jump to loopCond
            currentBlock.addInstruction(new BranchInst(currentBlock, null, loopCond, null));
            // loopMerge:
            currentBlock = loopMerge;
            irBuilder.setCurrentBlock(currentBlock);
        }
        return arrayHead;
    }
}
