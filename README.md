# Mx-Compiler

## Timeline

* 2020.1.11	Creating repo.
* 2020.1.16	Finish Mx.g4 v1.
* 2020.1.18	It is said that the assignment will be modified a lot🙃.
* 2020.1.21	Start building AST.
* 2020.1.22	Finish code of AST package. Start coding ASTBuilder.java.
* 2020.1.23	Finish building AST(Finish ASTBuilder.java).



## AST Structure
* - [x] ASTNode (location)
  * - [x] ProgramNode (programUnits)
  * - [x] TypeNode (identifier)
    * - [x] PrimitiveTypeNode (identifier = int / bool / String / void)
    * - [x] ClassTypeNode
    * - [x] ArrayTypeNode (baseType = , dims)
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

