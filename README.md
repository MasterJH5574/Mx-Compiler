# Mx-Compiler

## Timeline

* 2020.1.11	Create repo.
* 2020.1.16	Finish Mx.g4 v1.
* 2020.1.18	It is said that the assignment will be modified a lot🙃.
* 2020.1.21	Start building AST.
* 2020.1.22	Finish code of AST package. Start coding ASTBuilder.java.
* 2020.1.23	Finish building AST(Finish ASTBuilder.java).



## Parser

Using ANTLR4.

Mx.g4 ===> MxLexer.java, MxParser.java, MxVisitor.java, MxBaseVisitor.java



## AST

### Structure

* - [x] ASTNode (location)
  * - [x] ProgramNode (programUnits)
  * - [x] TypeNode (identifier)
    * - [x] PrimitiveTypeNode (identifier = int / bool / String / void)
    * - [x] ClassTypeNode
    * - [x] ArrayTypeNode (baseType = primitive type / class type, dims)
  * - [x] ProgramUnitNode
    * - [x] VarNodeList (varNodes)
    * - [x] VarNode (type, identifier, initExpr)
    * - [x] FunctionNode (type, identifier, parameters, statement)
    * - [x] ClassNode (identifier, varList, funcList)
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
  * - [x] ExprNode
    * - [x] PostfixExprNode (op, expr)
    * - [x] PrefixExprNode (op, expr)
    * - [x] BinaryExprNode (op, lhs, rhs)
    * - [x] NewExprNode (typeName, exprForDim, dim)
    * - [x] MemberExprNode (expr, identifier)
    * - [x] FuncCallExprNode (funcName, parameters)
    * - [x] SubscriptExprNode (name, index)
    * - [x] ThisExprNode
    * - [x] ConstExprNode
      * - [x] BoolLiteralNode (value)
      * - [x] IntLiteralNode (value)
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

