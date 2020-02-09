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
* 2020.2.7-2020.2.9	Write package IR.



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
        scopeStack.add(globalScope);
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

### Instructions

* - [ ] IRInstruction
  * - [ ] ReturnInst
  * - [ ] BranchInst
  * - [ ] BinaryOpInst
  * - [ ] AllocateInst
  * - [ ] LoadInst
  * - [ ] StoreInst
  * - [ ] GetElementPtrInst
  * - [ ] BitCastToInst
  * - [ ] IcmpInst
  * - [ ] PhiInst
  * - [ ] CallInst

### Type System

* - [ ] Type
  * - [ ] VoidType
  * - [ ] FunctionType
  * - [ ] IntegerType
  * - [ ] PointerType
  * - [ ] LabelType
  * - [ ] ArrayType
  * - [ ] StructureType

### Operand

* - [ ] Operand
  * - [ ] GlobalVariable
  * - [ ] Register
  * - [ ] Parameter
  * - [ ] Constant
    * - [ ] ConstInt
    * - [ ] ConstBool
    * - [ ] ConstString
    * - [ ] ConstNull


