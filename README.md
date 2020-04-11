# Mx-Compiler

## Timeline

* 2020.1.11	Create repo.
* 2020.1.16	Finish Mx.g4 v1.
* 2020.1.18	It is said that the assignment will be modified a lot🙃.
* 2020.1.21	Start building AST.
* 2020.1.22	Finish code of AST package. Start coding ASTBuilder.java.
* 2020.1.23	Finish building AST(Finish ASTBuilder.java).
* 2020.1.29	Start semantic analysis(so complexed...).
* 2020.1.30	Add ErrorHandler. Add Scope, TypeTable, package Type and package Entity. Start coding Checker.java.
* 2020.1.31	Continue semantic analysis. Finish variable resolver, type resolver and "void" checker in Checker.java.
* 2020.2.1	Continue semantic analysis(type check stage).
* 2020.2.2	Finish the basic code of semantic analysis
  * Built-in method of string unhandled.
  * To be debugged.
  * Update Mx.g4 and package Parser since ";" is required at the end of class definition.
* 2020.2.3	Continue semantic analysis.
  * Check return statement in functions with non-void return value type.
  * Check "int main()" and its return statement.
  * Handle built-in functions and methods.
  * Update rules of naming a class or entity.
* 2020.2.4	Debug. Finish semantic analysis.
  * Add MxErrorListener to lexer and parser.
  * Almost pass all the semantic test cases(90.56%). See [Pitfalls](#pitfalls) for detail.
* 2020.2.6	Learn LLVM.
* 2020.2.7-2020.2.11	Write package IR.
* 2020.2.12	Finish IRBuilder.
  * StringLiteral to be fixed.
  * To be debugged.
* 2020.2.13	Add interface IRVisitor and method accept(IRVisitor visitor).
* 2020.2.14	Implement IRPrinter. Add some assert statement to ensure correctness.
  * IRBuilder is still to be debugged.
  * Happy Valentine's Day!
* 2020.2.15	Debug. Pass all the semantic test cases. Fix [Pitfalls](#pitfalls) in semantic stage.
* 2020.2.16	Debug. Generate correct LLVM IR to pass all codegen test cases.
* 2020.2.17	Add class IRObject for use. Add def-use chains and use-def chain.
* 2020.2.18	Add DominatorTreeConstructor and [SSAConstructor](#ssa-constructionmem2reg-in-llvm-ir)(to be debugged).
* 2020.2.19	Add [CFGSimplifier](#cfg-simplification)(to be debugged).
  * I need to spend more time on TA's task of CS158...See you later.
* 2020.2.22	Debug. Fix bugs in CFGSimplifier.
* 2020.2.23	Debug. 
  * Fix bugs in DominatorTreeConstructor and add a CFG/Dominator Tree/Dominance Frontier printer.
  * Fix bugs in SSAConstructor and CFGSimplifier. Fix bugs when adding instructions and replacing uses. Add default value for return value in a function.
  * It can pass all codegen test with LLVM IR again by far.
* 2020.2.24	Debug.
  * Store default value to new allocated register so that no exception will be throwed when the use is before the def.
  * Fix a bug when removing a block from a function.
  * Fix a bug in CFGSimplifier so it can remove unreachable blocks correctly.
  * Fix a bug in SSAConstructor to collect all allocate instructions.
  * Fix bugs in NewArrayMalloc and IRBuilder to generate allocate and store instructions with correct BasicBlock.
  * It can generate LLVM IR for all semantic-pass test cases.
* 2020.2.25	Debug. Add [DeadCodeEliminator](#dead-code-elimination).
  * Replace `Set<IRInstruction> use` with `Map<IRInstruction, Integer> use`.
  * Fix a bug when adding a new branch to PhiInst(add use to operand and block).
* 2020.2.26	Add [SCCP](#sparse-conditional-constant-propagation). Remove some redundant visits from IRVisitor.
* 2020.2.27	Add something and debug.
  * Remove phi functions with single incoming value in CFGSimplifier.
  * Fix two bugs when merging blocks(removing single incoming value phi functions, remove uses of the merged block).
* 2020.2.28	Add [CSE](#common-subexpression-elimination)(without Alias Analysis).
* 2020.2.29	Overload `public Object clone()` for BasicBlock, IRInstruction and Register.
* 2020.3.1	Add InlineExpander(to be debugged).
* 2020.3.2	Add something and debug.
  * Add FunctionRemover to remove functions which are never called.
  * Debug. Fix bugs in InlineExpander and not it can pass codegen test cases.
* 2020.3.4	Revert two boolean methods...
* 2020.3.5	Add Andersen's Point To Analysis(to be debugged).
* 2020.3.8	Debug for Andersen. Add SideEffectChecker.
* 2020.3.9	Debug for Andersen. Use Andersen to improve CSE.
  * Improve Function.isNotFunctional().
* 2020.3.10	Fix bugs in SideEffectChecker and CFGSimplifier.
* 2020.3.11	Use SideEffectChecker to improve DCE.
* 2020.3.17	Add LoopAnalysis.	
  * Fix a bug when detecting side effect.
  * Fix bugs when replacing use. 
  * Improve SideEffectChecker to support "ignoreLoad".
* 2020.3.18	Improve LoopAnalysis. Add LICM.
* 2020.3.24	Add post-dominator analysis.
* 2020.3.25	Improve DCE to real ADCE. Update LoopAnalysis.
* 2020.3.26	Try InstructionCombiner.
* 2020.3.27	Improve getNameWithoutDot() for Register and BasicBlock.
* 2020.3.28	Give a lecture to beginner.
* 2020.3.29	Add InstructionCombiner.
* 2020.4.2	Pass semantic tests on OnlineJudge.
* 2020.4.11	Modify IRPrinter. Add SSADestructor.



## Parser

Using ANTLR4.

Mx.g4 ===> MxLexer.java, MxParser.java, MxVisitor.java, MxBaseVisitor.java



## AST

### class ASTNode Structure

* - [x] ASTNode (location, scope)
  * - [x] ProgramNode (programUnits)
  * - [x] TypeNode (identifier)
    * - [x] PrimitiveTypeNode (identifier = int / bool / string / void)
    * - [x] ClassTypeNode
    * - [x] ArrayTypeNode (baseType = primitive type / class type, dims)
  * - [x] ProgramUnitNode
    * - [x] VarNodeList (varNodes)
    * - [x] VarNode (type, identifier, initExpr)
    * - [x] FunctionNode (type, identifier, parameters, statement)
    * - [x] ClassNode (identifier, varList, constructor, funcList)
  * - [x] StmtNode
    * - [x] BlockNode (statements)
    * - [x] VarDeclStmtNode (varList)
    * - [x] IfStmtNode (cond, thenBody, elseBody)
    * - [x] WhileStmt (cond, body)
    * - [x] ForStmtNode (init, cond, step, body)
    * - [x] ReturnStmtNode (returnValue)
    * - [x] BreakStmtNode
    * - [x] ContinueStmtNode
    * - [x] ExprStmtNode (expr)
  * - [x] ExprNode (text, entity, lvalue, type)
    * - [x] PostfixExprNode (op, expr)
    * - [x] PrefixExprNode (op, expr)
    * - [x] BinaryExprNode (op, lhs, rhs)
    * - [x] NewExprNode (typeName, exprForDim, dim)
    * - [x] MemberExprNode (expr, identifier)
    * - [x] FuncCallExprNode (funcName, parameters)
    * - [x] SubscriptExprNode (name, index, dim)
    * - [x] ThisExprNode
    * - [x] ConstExprNode
      * - [x] BoolLiteralNode (value)
      * - [x] IntLiteralNode (**Long** value)
      * - [x] StringLiteralNode (value)
      * - [x] NullLiteralNode
    * - [x] IdExprNode (identifier)

### Build AST in ASTBuilder.java

```java
public class ASTBuilder extends MxBaseVisitor<ASTNode> {
    @Override
    public ASTNode visitProgram(MxParser.ProgramContext ctx) {
        // return ProgramNode
        ArrayList<ProgramUnitNode> programUnits = new ArrayList<>();
        for (var programUnit : ctx.programUnit()) {
            ASTNode unit = visit(programUnit);
            if (unit instanceof VarNodeList)
                programUnits.addAll(((VarNodeList) unit).getVarNodes());
            else if (unit != null)
                programUnits.add((ProgramUnitNode) unit);
            // else do nothing
        }
        return new ProgramNode(new Location(ctx.getStart()), programUnits);
    }
    
    // Override other methods...
}
```



## Semantic Analysis

### Scope and Entity

#### class Entity Structure

* - [x] Entity (name, referred)
  * - [x] FunctionEntity (returnType, parameters, bodyStmt, entityType)
  * - [x] VariableEntity (type, initExpr, entityType)

#### Scope

```java
public class Scope {
    public enum ScopeType {
        programScope, classScope, functionScope, blockScope, loopScope
    }

    private Scope parentScope;
    private ArrayList<Scope> childrenScope;

    private Map<String, Entity> entities;
    private ScopeType scopeType;
    private TypeNode functionReturnType;
    private Type classType;
    
    // methods such as "declareEntity()"...
}
```

#### ~~Rules of Naming~~

~~Name of global/local variables, parameters, members and methods can't be the same with name of functions and classes.~~

### Type and TypeTable

#### class Type Structure

* - [x] Type (name, size)			**Member "size" is to be set later.**
  * - [x] IntType
  * - [x] BoolType
  * - [x] StringType (methods)
  * - [x] VoidType
  * - [x] NullType
  * - [x] ClassType (members, constructor, methods)
  * - [x] ArrayType (baseType, dims, methods)
  * - [x] MethodType (Type)
    * Used for method call such as `obj.method(a, b, c)`, `str.substring(l, r)`, `arr.size()`.

#### TypeTable

```java
public class TypeTable {
    private Map<TypeNode, Type> typeTable;

    public void put(TypeNode typeNode, Type type) {
        // put baseType in typeTable
        // check duplicate type
    }
    
    public Type get(TypeNode typeNode) {
        // if typeNode instance of ArrayTypeNode...
        // else...
    }
}
```

### Checker

```java
// Semantic checker

public class Checker implements ASTVisitor {
    private Scope globalScope;
    private Stack<Scope> scopeStack;
    private TypeTable typeTable;
    private ErrorHandler errorHandler;
    
    @Override
    public void visit(ProgramNode node) throws CompilationError {
        globalScope = new Scope(null, Scope.ScopeType.programScope,
                null, null);
        scopeStack.push(globalScope);
        node.setScope(globalScope);

        globalScope.addBuiltInFunction();

        boolean error = false; // error may be set to "true" in the following steps

        // Step 1: define classes
        // Step 2: define functions
        // Step 3: resolve in order
        // Step 4: check "int main()"

        scopeStack.pop();

        if (error)
            throw new CompilationError();
    }
    
    // Override other methods...
}
```

### Pitfalls

Cannot check whether there is a return statement in semantic stage. 

For example, in semantic test case basic-19.mx:

```c++
int foo(int a) {
    if (a == 1) return 0;
    else if (a > 5) {
        if (a < 10) {
            if (a > 8) {
                if (a <= 9) {
                    return "hello";
                }
            }
        }
    }else {
        return 1;
    }
}
```

Maybe I can check return statement in IR stage.

### ErrorHandler

See [ErrorHandler.java](https://github.com/MasterJH5574/Mx-Compiler/blob/master/src/MxCompiler/Utilities/ErrorHandler.java) for details.

```java
public class ErrorHandler {
    private PrintStream printStream;
    private int errorCnt;
    private int warningCnt;
    
    public ErrorHandler() {
        printStream = System.err;
        errorCnt = 0;
        warningCnt = 0;
    }
    
    // ...
}
```

#### What is the advantage of ErrorHandler?

Collect **as much errors as possible** except errors occurred in an expression. Print the errors together.

It seems user-friendly.



## Intermediate Representation

CFG + LLVM IR

Top Abstract Class: IRObject

### Basic Components

Module  --  Function -- BasicBlock -- IRInstruction

### Instructions

* - [x] IRInstruction (basicBlock, instPrev, instNext)
  * - [x] ReturnInst (type, returnValue)
  * - [x] BranchInst (cond, thenBlock, elseBlock)
  * - [x] BinaryOpInst (op, lhs, rhs, result)
  * - [x] AllocateInst (result, type)
  * - [x] LoadInst (type, pointer, result)
  * - [x] StoreInst (value, pointer)
  * - [x] GetElementPtrInst (pointer, index, result)
  * - [x] BitCastToInst (src, objectType, result)
  * - [x] IcmpInst (operator, irType, op1, op2, result)
  * - [x] PhiInst (branch, result)
  * - [x] CallInst (function, parameter, result)
  * - [x] MoveInst(source, result) ***Only used for SSA Destruction!***
  * - [x] ParallelCopy(moves) ***Only used for SSA Destruction!***

### Type System

* - [x] IRType
  * - [x] VoidType
  * - [x] FunctionType (returnType, parameterList)
  * - [x] IntegerType (bitWidth)
  * - [x] PointerType (baseType)
  * - [x] ArrayType (size, type)
  * - [x] StructureType (name, memberList)

#### IRTypeTable

Map "MxCompiler.Type" to "MxCompiler.IR.TypeSystem.IRType".


### Operand

* - [x] Operand (type)
  * - [x] GlobalVariable (name, init)
  * - [x] Register (name)
  * - [x] Parameter (name)
  * - [x] Constant
    * - [x] ConstInt (value)
    * - [x] ConstBool (value)
    * - [x] ConstString (value)
    * - [x] ConstNull



## Optimization

### CFG Simplification

By far, **CFG simplification** consists of **2 steps**.

1. Simplify branches.
2. Merge blocks which are linked with redundant unconditional branch.

### SSA Construction(Mem2Reg in LLVM IR)

Note that LLVM IR is **in SSA form for registers,** but **not for memory**. So there are lots memory access in original LLVM IR. Hence we need to perform a SSA Construction **for memory** so that for all alloca instructions, their corresponding load/store instructions can be removed.

#### Algorithm

*See Chapter 3 of [SSA Book](http://ssabook.gforge.inria.fr/latest/book.pdf) for details.*

Step 1: Construct **Dominator Tree** and compute the **Dominance Frontier** for each node in CFG.

Step 2: Regard <u>alloca instructions</u> as variables, its <u>load instructions</u> as uses, its <u>store instructions</u> as definitions.

Step 3: Insert **Phi-function** for each variable to its **Iterated Dominance Frontier(DF+)**.

Step 4: "Rename". **Replace uses** of load instruction. Remove alloca, load and store instructions.

### Dead Code Elimination

**Aggressive Dead Code Elimination**: Assume a statement is **dead** until proven otherwise.

```java
public class DeadCodeEliminator extends Pass {
    private boolean deadCodeElimination(Function function) {
        Set<IRInstruction> live = new HashSet<>();
        Queue<IRInstruction> queue = new LinkedList<>();
        for (BasicBlock block : function.getBlocks())
            addLiveInstructions(block, live, queue);

        while (!queue.isEmpty()) {
            IRInstruction instruction = queue.poll();
            instruction.markUseAsLive(live, queue);
            for (BasicBlock predecessor : instruction.getBasicBlock().getPredecessors()) {
                if (!live.contains(predecessor.getInstTail())) {
                    live.add(predecessor.getInstTail());
                    queue.offer(predecessor.getInstTail());
                }
            }
        }

        boolean changed = false;
        for (BasicBlock block : function.getBlocks())
            changed |= removeDeadInstructions(block, live);
        return changed;
    }
}
```

#### Conditi+on for "Live" instruction

1. I/O
2. Store instructions
3. Return instructions.
4. Instructions which call a function with potential side effect.

### Sparse Conditional Constant Propagation

For SCCP algorithm, see Tiger Book section 19.3: conditional constant propagation.

1. Assume blocks are **unexecutable** until proven otherwise. When visiting a branch instruction, mark its successor(s) executable according to the condition of the branch.
2. Assume registers are undefined. There are 3 statuses for registers: *undefined*, *constant*, and *multiDefined*. *Undefined* is the lowest status and *multiDefined* is the highest status. When visiting instructions, each time one can promote the status of a register to a higher status.
3. Use two work lists (= queue) to store registers and blocks respectively. Entrance block is added to the queue of blocks at first.
4. When popping a block out of the queue, visit all its instructions.
5. When visiting an instruction, try to promote the status of the result according to the rules.
6. Once the status of a register is promoted, push the register into the queue of registers.
7. When popping a register out of the queue, visit all its use.

### Common Subexpression Elimination

**Need Dominance Analysis.**

Use a map to collect all different expressions appeared.

If instruction k dominates instruction l, and k and l share the same expression, then there is no need to recalculate the expression for k. It is enough to replace the use of the result of instruction l with the result of instruction k.

**Since I don't implement Alias Analysis, load instructions cannot be optimized by CSE.**

