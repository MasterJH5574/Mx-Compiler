package MxCompiler.Frontend;


import MxCompiler.AST.*;
import MxCompiler.Parser.MxBaseVisitor;
import MxCompiler.Parser.MxParser;
import MxCompiler.Utilities.CompilationError;
import MxCompiler.Utilities.ErrorHandler;
import MxCompiler.Utilities.Location;

import java.util.ArrayList;

public class ASTBuilder extends MxBaseVisitor<ASTNode> {
    ErrorHandler errorHandler;

    public ASTBuilder(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

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

    @Override
    public ASTNode visitProgramUnit(MxParser.ProgramUnitContext ctx) {
        // return ProgramUnitNode
        if (ctx.functionDef() != null) {
            return visit(ctx.functionDef());
        } else if (ctx.classDef() != null) {
            return visit(ctx.classDef());
        } else if (ctx.varDecl() != null) {
            return visit(ctx.varDecl());
        } else // ";"
            return null;
    }

    @Override
    public ASTNode visitFunctionDef(MxParser.FunctionDefContext ctx) {
        // return FunctionNode
        TypeNode type;
        if (ctx.type() != null)
            type = (TypeNode) visit(ctx.type());
        else // void type
            type = new PrimitiveTypeNode(new Location(ctx.VOID().getSymbol()), "void");
        String identifier = ctx.IDENTIFIER().getText();
        ArrayList<VarNode> parameters = new ArrayList<>();
        if (ctx.parameterList() != null)
            parameters = ((VarNodeList) visit(ctx.parameterList())).getVarNodes();
        StmtNode statement = (StmtNode) visit(ctx.block());
        return new FunctionNode(new Location(ctx.getStart()), type, identifier, parameters, statement);
    }

    @Override
    public ASTNode visitClassDef(MxParser.ClassDefContext ctx) {
        // return ClassNode
        String identifier = ctx.IDENTIFIER().getText();
        ArrayList<VarNode> varList = new ArrayList<>();
        for (var varDecl : ctx.varDecl()) {
            VarNodeList varNodeList = (VarNodeList) visit(varDecl);
            varList.addAll(varNodeList.getVarNodes());
        }

        ArrayList<FunctionNode> funcList = new ArrayList<>();
        for (var functionDef : ctx.functionDef())
            funcList.add((FunctionNode) visit(functionDef));

        FunctionNode constructor = null;
        if (ctx.constructorDef().size() > 1)
            errorHandler.error(new Location(ctx.constructorDef(0).getStart()),
                    "Class \"" + identifier + "\" has multiple constructors.");
        for (var constructorDef : ctx.constructorDef()) {
            if (!constructorDef.IDENTIFIER().getText().equals(identifier))
                errorHandler.error(new Location(constructorDef.getStart()),
                        "Unknown constructor \"" + identifier + "()\".");
            else
                constructor = (FunctionNode) visit(constructorDef);
        }

        return new ClassNode(new Location(ctx.getStart()), identifier, varList, constructor, funcList);
    }

    @Override
    public ASTNode visitVarDecl(MxParser.VarDeclContext ctx) {
        // return VarNodeList
        TypeNode type = (TypeNode) visit(ctx.type());
        ArrayList<VarNode> varNodes = ((VarNodeList) visit(ctx.varDeclList())).getVarNodes();
        for (var varNode : varNodes)
            varNode.setType(type);
        return new VarNodeList(new Location(ctx.getStart()), varNodes);
    }

    @Override
    public ASTNode visitConstructorDef(MxParser.ConstructorDefContext ctx) {
        // return FunctionNode
        TypeNode type = new PrimitiveTypeNode(new Location(ctx.getStart()), "void");
        String identifier = ctx.IDENTIFIER().getText();
        ArrayList<VarNode> parameters = new ArrayList<>();
        if (ctx.parameterList() != null)
            parameters = ((VarNodeList) visit(ctx.parameterList())).getVarNodes();
        StmtNode statement = (StmtNode) visit(ctx.block());
        return new FunctionNode(new Location(ctx.getStart()), type, identifier, parameters, statement);
    }

    @Override
    public ASTNode visitType(MxParser.TypeContext ctx) {
        // return TypeNode
        if (ctx.nonArrayType() != null)
            return visit(ctx.nonArrayType());
        else // ArrayType
            return new ArrayTypeNode(new Location(ctx.getStart()), (TypeNode) visit(ctx.type()));
    }

    @Override
    public ASTNode visitNonArrayType(MxParser.NonArrayTypeContext ctx) {
        // return PrimitiveTypeNode or ClassTypeNode
        Location location = new Location(ctx.getStart());
        if (ctx.BOOL() != null)
            return new PrimitiveTypeNode(location, "bool");
        else if (ctx.INT() != null)
            return new PrimitiveTypeNode(location, "int");
        else if (ctx.STRING() != null)
            return new PrimitiveTypeNode(location, "string");
        else // ctx.IDENTIFIER() != null
            return new ClassTypeNode(location, ctx.IDENTIFIER().getText());
    }

    @Override
    public ASTNode visitParameterList(MxParser.ParameterListContext ctx) {
        // return VarNodeList
        ArrayList<VarNode> varNodes = new ArrayList<>();
        for (var parameter : ctx.parameter())
            varNodes.add((VarNode) visit(parameter));
        return new VarNodeList(new Location(ctx.getStart()), varNodes);
    }

    @Override
    public ASTNode visitParameter(MxParser.ParameterContext ctx) {
        // return VarNode
        TypeNode type = (TypeNode) visit(ctx.type());
        String identifier = ctx.IDENTIFIER().getText();
        return new VarNode(new Location(ctx.getStart()), type, identifier, null);
    }

    @Override
    public ASTNode visitVarDeclList(MxParser.VarDeclListContext ctx) {
        // return VarNodeList
        ArrayList<VarNode> varNodes = new ArrayList<>();
        for (var varDeclSingle : ctx.varDeclSingle())
            varNodes.add((VarNode) visit(varDeclSingle));
        return new VarNodeList(new Location(ctx.getStart()), varNodes);
    }

    @Override
    public ASTNode visitVarDeclSingle(MxParser.VarDeclSingleContext ctx) {
        // return VarNode
        Location location = new Location(ctx.getStart());
        TypeNode type = new PrimitiveTypeNode(location, "#VarDeclSingleToBeSet#");
        String identifier = ctx.IDENTIFIER().getText();
        ExprNode initExpr = ctx.expr() != null ? (ExprNode) visit(ctx.expr()) : null;
        return new VarNode(location, type, identifier, initExpr);
    }

    @Override
    public ASTNode visitBlock(MxParser.BlockContext ctx) {
        // return BlockNode
        ArrayList<StmtNode> statements = new ArrayList<>();
        for (var statement : ctx.statement()) {
            StmtNode stmtNode = (StmtNode) visit(statement);
            if (stmtNode != null) // EmptyStmt -> null
                statements.add(stmtNode);
        }
        return new BlockNode(new Location(ctx.getStart()), statements);
    }

    @Override
    public ASTNode visitBlockStmt(MxParser.BlockStmtContext ctx) {
        // return BlockNode(StmtNode)
        return visit(ctx.block());
    }

    @Override
    public ASTNode visitVarDeclStmt(MxParser.VarDeclStmtContext ctx) {
        // return VarDeclStmtNode(StmtNode)
        ArrayList<VarNode> varNodes = ((VarNodeList) visit(ctx.varDecl())).getVarNodes();
        return new VarDeclStmtNode(new Location(ctx.getStart()), varNodes);
    }

    @Override
    public ASTNode visitIfStmt(MxParser.IfStmtContext ctx) {
        // return IfStmtNode(StmtNode)
        ExprNode cond = (ExprNode) visit(ctx.expr());
        StmtNode thenBody = (StmtNode) visit(ctx.statement(0));
        StmtNode elseBody = ctx.statement(1) != null ? (StmtNode) visit(ctx.statement(1)) : null;
        return new IfStmtNode(new Location(ctx.getStart()), cond, thenBody, elseBody);
    }

    @Override
    public ASTNode visitWhileStmt(MxParser.WhileStmtContext ctx) {
        // return WhileStmtNode(StmtNode)
        ExprNode cond = (ExprNode) visit(ctx.expr());
        StmtNode body = (StmtNode) visit(ctx.statement());
        return new WhileStmtNode(new Location(ctx.getStart()), cond, body);
    }

    @Override
    public ASTNode visitForStmt(MxParser.ForStmtContext ctx) {
        // return ForStmtNode(StmtNode)
        ExprNode init = ctx.init != null ? (ExprNode) visit(ctx.init) : null;
        ExprNode cond = ctx.cond != null ? (ExprNode) visit(ctx.cond) : null;
        ExprNode step = ctx.step != null ? (ExprNode) visit(ctx.step) : null;
        StmtNode body = (StmtNode) visit(ctx.statement());
        return new ForStmtNode(new Location(ctx.getStart()), init, cond, step, body);
    }

    @Override
    public ASTNode visitReturnStmt(MxParser.ReturnStmtContext ctx) {
        // return ReturnStmtNode(StmtNode)
        ExprNode returnValue = ctx.expr() != null ? (ExprNode) visit(ctx.expr()) : null;
        return new ReturnStmtNode(new Location(ctx.getStart()), returnValue);
    }

    @Override
    public ASTNode visitBreakStmt(MxParser.BreakStmtContext ctx) {
        // return BreakStmtNode(StmtNode)
        return new BreakStmtNode(new Location(ctx.getStart()));
    }

    @Override
    public ASTNode visitContinueStmt(MxParser.ContinueStmtContext ctx) {
        // return ContinueStmtNode(StmtNode)
        return new ContinueStmtNode(new Location(ctx.getStart()));
    }

    @Override
    public ASTNode visitEmptyStmt(MxParser.EmptyStmtContext ctx) {
        return null;
    }

    @Override
    public ASTNode visitExprStmt(MxParser.ExprStmtContext ctx) {
        // return ExprStmtNode
        ExprNode expr = (ExprNode) visit(ctx.expr());
        return new ExprStmtNode(new Location(ctx.getStart()), expr);
    }

    @Override
    public ASTNode visitNewExpr(MxParser.NewExprContext ctx) {
        // return NewExprNode
        NewExprNode newExprNode = (NewExprNode) visit(ctx.creator());
        return new NewExprNode(newExprNode.getLocation(), ctx.getText(),
                newExprNode.getBaseType(), newExprNode.getExprForDim(), newExprNode.getDim());
    }

    @Override
    public ASTNode visitPrefixExpr(MxParser.PrefixExprContext ctx) {
        // return PrefixExprNode
        PrefixExprNode.Operator op = null;
        String opCtx = ctx.op.getText();
        switch (opCtx) {
            case "++":
                op = PrefixExprNode.Operator.preInc;
                break;
            case "--":
                op = PrefixExprNode.Operator.preDec;
                break;
            case "+":
                op = PrefixExprNode.Operator.signPos;
                break;
            case "-":
                op = PrefixExprNode.Operator.signNeg;
                break;
            case "!":
                op = PrefixExprNode.Operator.logicalNot;
                break;
            case "~":
                op = PrefixExprNode.Operator.bitwiseComplement;
                break;
        }
        ExprNode expr = (ExprNode) visit(ctx.expr());
        return new PrefixExprNode(new Location(ctx.getStart()), ctx.getText(), op, expr);
    }

    @Override
    public ASTNode visitThisExpr(MxParser.ThisExprContext ctx) {
        // return ThisExprNode
        return new ThisExprNode(new Location(ctx.getStart()), ctx.getText());
    }

    @Override
    public ASTNode visitSubscriptExpr(MxParser.SubscriptExprContext ctx) {
        // return SubscriptExprNode
        ExprNode name = (ExprNode) visit(ctx.expr(0));
        ExprNode index = (ExprNode) visit(ctx.expr(1));
        return new SubscriptExprNode(new Location(ctx.getStart()), ctx.getText(), name, index);
    }

    @Override
    public ASTNode visitMemberExpr(MxParser.MemberExprContext ctx) {
        // return MemberExprNode
        ExprNode expr = (ExprNode) visit(ctx.expr());
        String identifier = ctx.IDENTIFIER().getText();
        return new MemberExprNode(new Location(ctx.getStart()), ctx.getText(), expr, identifier);
    }

    @Override
    public ASTNode visitBinaryExpr(MxParser.BinaryExprContext ctx) {
        // return BinaryExprNode
        BinaryExprNode.Operator op = null;
        String opCtx = ctx.op.getText();
        switch (opCtx) {
            case "*":
                op = BinaryExprNode.Operator.mul;
                break;
            case "/":
                op = BinaryExprNode.Operator.div;
                break;
            case "%":
                op = BinaryExprNode.Operator.mod;
                break;
            case "+":
                op = BinaryExprNode.Operator.add;
                break;
            case "-":
                op = BinaryExprNode.Operator.sub;
                break;
            case "<<":
                op = BinaryExprNode.Operator.shiftLeft;
                break;
            case ">>":
                op = BinaryExprNode.Operator.shiftRight;
                break;
            case "<":
                op = BinaryExprNode.Operator.less;
                break;
            case ">":
                op = BinaryExprNode.Operator.greater;
                break;
            case "<=":
                op = BinaryExprNode.Operator.lessEqual;
                break;
            case ">=":
                op = BinaryExprNode.Operator.greaterEqual;
                break;
            case "==":
                op = BinaryExprNode.Operator.equal;
                break;
            case "!=":
                op = BinaryExprNode.Operator.notEqual;
                break;
            case "&":
                op = BinaryExprNode.Operator.bitwiseAnd;
                break;
            case "^":
                op = BinaryExprNode.Operator.bitwiseXor;
                break;
            case "|":
                op = BinaryExprNode.Operator.bitwiseOr;
                break;
            case "&&":
                op = BinaryExprNode.Operator.logicalAnd;
                break;
            case "||":
                op = BinaryExprNode.Operator.logicalOr;
                break;
            case "=":
                op = BinaryExprNode.Operator.assign;
                break;
        }
        ExprNode lhs = (ExprNode) visit(ctx.src1);
        ExprNode rhs = (ExprNode) visit(ctx.src2);
        return new BinaryExprNode(new Location(ctx.getStart()), ctx.getText(), op, lhs, rhs);
    }

    @Override
    public ASTNode visitPostfixExpr(MxParser.PostfixExprContext ctx) {
        // return PostfixExprNode
        PostfixExprNode.Operator op = null;
        String opCtx = ctx.op.getText();
        switch (opCtx) {
            case "++":
                op = PostfixExprNode.Operator.postInc;
                break;
            case "--":
                op = PostfixExprNode.Operator.postDec;
                break;
        }
        ExprNode expr = (ExprNode) visit(ctx.expr());
        return new PostfixExprNode(new Location(ctx.getStart()), ctx.getText(), op, expr);
    }

    @Override
    public ASTNode visitFuncCallExpr(MxParser.FuncCallExprContext ctx) {
        // return FuncCallExprNode
        Location location = new Location(ctx.getStart());
        ExprNode funcName = (ExprNode) visit(ctx.expr());
        if (ctx.exprList() == null)
            return new FuncCallExprNode(location, ctx.getText(), funcName, new ArrayList<>());
        else {
            FuncCallExprNode funcCallExprNode = (FuncCallExprNode) visit(ctx.exprList());
            return new FuncCallExprNode(location, ctx.getText(), funcName, funcCallExprNode.getParameters());
        }
    }

    @Override
    public ASTNode visitSubExpr(MxParser.SubExprContext ctx) {
        // return ExprNode
        return visit(ctx.expr());
    }

    @Override
    public ASTNode visitConstExpr(MxParser.ConstExprContext ctx) {
        // return ConstExprNode
        return visit(ctx.constant());
    }

    @Override
    public ASTNode visitIdExpr(MxParser.IdExprContext ctx) {
        // return IdExprNode
        String identifier = ctx.IDENTIFIER().getText();
        return new IdExprNode(new Location(ctx.getStart()), ctx.getText(), identifier);
    }

    @Override
    public ASTNode visitExprList(MxParser.ExprListContext ctx) {
        // return FuncCallExprNode
        ArrayList<ExprNode> parameters = new ArrayList<>();
        for (var expr : ctx.expr())
            parameters.add((ExprNode) visit(expr));
        return new FuncCallExprNode(new Location(ctx.getStart()), ctx.getText(), null, parameters);
    }

    @Override
    public ASTNode visitWrongCreator(MxParser.WrongCreatorContext ctx) {
        // return NewExprNode
        TypeNode baseType = (TypeNode) visit(ctx.nonArrayType());
        errorHandler.error(new Location(ctx.getStart()), "Invalid syntax \"" + ctx.getText() + "\".");
        return new NewExprNode(new Location(ctx.getStart()), ctx.getText(), baseType, null, -1);
    }

    @Override
    public ASTNode visitArrayCreator(MxParser.ArrayCreatorContext ctx) {
        // return NewExprNode
        TypeNode baseType = (TypeNode) visit(ctx.nonArrayType());

        int dim = 0;
        for (var child : ctx.children)
            if (child.getText().equals("["))
                dim++;

        ArrayList<ExprNode> exprForDim = new ArrayList<>();
        for (var expr : ctx.expr())
            exprForDim.add((ExprNode) visit(expr));

        return new NewExprNode(new Location(ctx.getStart()), ctx.getText(), baseType, exprForDim, dim);
    }

    @Override
    public ASTNode visitClassCreator(MxParser.ClassCreatorContext ctx) {
        // return NewExprNode
        TypeNode baseType = (TypeNode) visit(ctx.nonArrayType());
        return new NewExprNode(new Location(ctx.getStart()), ctx.getText(), baseType, new ArrayList<>(), 0);
    }

    @Override
    public ASTNode visitNaiveCreator(MxParser.NaiveCreatorContext ctx) {
        // return NewExprNode
        TypeNode baseType = (TypeNode) visit(ctx.nonArrayType());
        return new NewExprNode(new Location(ctx.getStart()), ctx.getText(), baseType, new ArrayList<>(), 0);
    }

    @Override
    public ASTNode visitConstant(MxParser.ConstantContext ctx) {
        // return ConstExprNode
        Location location = new Location(ctx.getStart());
        String value = ctx.getText();
        if (ctx.BoolLITERAL() != null)
            return new BoolLiteralNode(location, ctx.getText(), value.equals("true"));
        else if (ctx.IntegerLITERAL() != null)
            return new IntLiteralNode(location, ctx.getText(), Long.parseLong(value));
        else if (ctx.StringLITERAL() != null)
            return new StringLiteralNode(location, ctx.getText(), value);
        else // ctx.NULL != null
            return new NullLiteralNode(location, ctx.getText());
    }
}
