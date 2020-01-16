grammar Mx;

@header {
package MxCompiler.Parser;
}


program
    :   programUnit*
    ;

programUnit
    :   functionDef     // check whether there is "main()" in the semantic stage
    |   classDef
    |   varDecl
    |   ';'             // empty statement, undefined
    ;


// ------ DEFINITION & DECLARATION ------
functionDef
    :   (type | VOID) IDENTIFIER '(' parameterList? ')' block
    ;

classDef
    :   CLASS IDENTIFIER '{' (varDecl | functionDef | constructorDef)* '}'  // check numbers of
                                                                            // construcor in the semantic stage
    ;

varDecl
    :   type varDeclList ';'
    ;

constructorDef  // Todo: constructor exists?
    :   IDENTIFIER '(' parameterList? ')' block
    ;


type:   type '[' ']'
    |   nonArrayType
    ;

nonArrayType
    :   BOOL
    |   INT
    |   STRING
    |   IDENTIFIER      // class name
    ;


parameterList   // used for function definition, not function call(see exprList)
    :   parameter (',' parameter)*
    ;

parameter
    :   type IDENTIFIER
    ;

varDeclList
    :   varDeclSingle (',' varDeclSingle)*
    ;

varDeclSingle
    :   IDENTIFIER ('=' expr)?
    ;


// ------ BLOCK & STATEMENT ------
block
    : '{' statement* '}'
    ;

statement
    :   block                                               #blockStmt
    |   varDecl                                             #varDeclStmt
    |   IF '(' expr ')' statement (ELSE statement)?         #ifStmt
    |   WHILE '(' expr ')' statement                        #whileStmt
    |   FOR '(' init=expr? ';'
                cond=expr? ';'
                step=expr? ')' statement                    #forStmt
    |   RETURN expr? ';'                                    #returnStmt
    |   BREAK ';'                                           #breakStmt
    |   CONTINUE ';'                                        #continueStmt
    |   ';'                                                 #emptyStmt
    |   expr ';'                                            #exprStmt
    ;


// ------ EXPRESSION ------
expr:   expr op=('++' | '--')                               #postfixExpr
    |   <assoc=right> NEW creator                           #newExpr
    |   expr '.' IDENTIFIER                                 #memberExpr
    |   expr '(' exprList? ')'                              #funcCallExpr
    |   expr '[' expr ']'                                   #subscriptExpr
    |   <assoc=right> op=('++' | '--') expr                 #prefixExpr
    |   <assoc=right> op=( '+' | '-' ) expr                 #prefixExpr
    |   <assoc=right> op=( '!' | '~' ) expr                 #prefixExpr
    |   src1=expr op=('*' | '/' | '%') src2=expr            #binaryExpr
    |   src1=expr op=('+' | '-') src2=expr                  #binaryExpr
    |   src1=expr op=('<<' | '>>') src2=expr                #binaryExpr
    |   src1=expr op=('<' | '>' | '<=' | '>=') src2=expr    #binaryExpr
    |   src1=expr op=('==' | '!=') src2=expr                #binaryExpr
    |   src1=expr op='&' src2=expr                          #binaryExpr
    |   src1=expr op='^' src2=expr                          #binaryExpr
    |   src1=expr op='|' src2=expr                          #binaryExpr
    |   src1=expr op='&&' src2=expr                         #binaryExpr
    |   src1=expr op='||' src2=expr                         #binaryExpr
    |   <assoc=right> src1=expr op='=' src2=expr            #binaryExpr
    |   '(' expr ')'                                        #subExpr
    |   THIS                                                #thisExpr
    |   constant                                            #constExpr
    |   IDENTIFIER                                          #idExpr
    ;

exprList
    :   expr (',' expr)*
    ;

creator
    :   nonArrayType ('[' expr ']')*('[' ']')+('[' expr ']')+   #wrongCreator
    |   nonArrayType ('[' expr ']')+('[' ']')*                  #arrayCreator
    |   nonArrayType '(' ')'                                    #classCreator
    |   nonArrayType                                            #naiveCreator
    ;

constant
    :   BoolLITERAL
    |   IntegerLITERAL
    |   StringLITERAL
    |   NULL
    ;


BoolLITERAL: TRUE | FALSE;
IntegerLITERAL: '0' | [1-9][0-9]*;
StringLITERAL: '"' (ESC|.)*? '"';
fragment
ESC: '\\"' | '\\n' | '\\\\';

// ------ RESERVED WORDS ------
INT:    'int';
BOOL:   'bool';
STRING: 'string';
NULL:   'null';
VOID:   'void';
TRUE:   'true';
FALSE:  'false';
IF:     'if';
ELSE:   'else';
FOR:    'for';
WHILE:  'while';
BREAK:  'break';
CONTINUE:'continue';
RETURN: 'return';
NEW:    'new';
CLASS:  'class';
THIS:   'this';

IDENTIFIER: [a-zA-Z][a-zA-Z_0-9]*;


// ------ SKIPS ------
Whitespace
    :   [ \t\n\r]+  -> skip
    ;

Newline
    :   (   '\r' '\n'?
        |   '\n'
        )   -> skip
    ;

BlockComment // undefined, be cautious
    :   '/*' .*? '*/'   -> skip
    ;

LineComment
    :   '//' ~[\r\n]*   -> skip
    ;