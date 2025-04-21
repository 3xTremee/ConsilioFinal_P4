grammar Consilio;
/*
program
    : domain EOF
    | problem EOF
    ;
*/

program
    : domain problem EOF
    ;

/*
 * Parser Rules
 */

domain
    : DEFINE DOMAIN IDENTIFIER type* action*
    ;

problem
    : DEFINE PROBLEM IDENTIFIER IMPORT IDENTIFIER objects init goal
    ;

type
    : TYPE IDENTIFIER LBRACE attribute* RBRACE
    ;

attribute
    : IDENTIFIER COLON value SEMI
    ;

value
    : valueType                 #baseValue
    | valueType OR value        #orValue
    | valueType LSQUARE RSQUARE #arrayValue            // Var tidligere IDENTIFIER LSQUARE RSQUARE. Men så virker location: int[] IKKE. Der ændret til valueType.
    ;

valueType: INT | BOOLEAN | IDENTIFIER;

action
    : ACTION IDENTIFIER LPAREN parameter RPAREN LBRACE statement RBRACE
    ;

parameter
    : IDENTIFIER IDENTIFIER (COMMA parameter)?
    ;

/*
actionBody
    : ifBlock
    | (statement SEMI)*
    ;
*/

objects
    : OBJECTS LBRACE objectBody RBRACE
    ;

objectBody
    : (arrayInitializer SEMI)*
    ;

arrayInitializer
    : IDENTIFIER IDENTIFIER LSQUARE RSQUARE ASSIGN LBRACE arrayBody RBRACE
    ;

arrayBody
    : IDENTIFIER (COMMA arrayBody)?
    ;

init
    : INITIALSTATE LBRACE statementList? RBRACE //var før (statement)*
    ;

goal
    : GOALSTATE LBRACE (expression SEMI)* RBRACE
    ;

ifBlock
    : IF LPAREN expression RPAREN LBRACE statementList RBRACE // var før statement (uden List)
    ;

statement
    : ifBlock                   #ifStatement
    | assignment                #assignmentStatement // var før assignment SEMI
//    | statement statement       #statementConcatination
//    | expression        #expressionStatement
//    | SEMI              #semiColon
    ;

statementList
    : (statement SEMI)+
    ;

assignment
    : IDENTIFIER ASSIGN expression       #identifierAssignment //Vi kan ikke huske hvorfor denne var med. Eventuelt slet til sidst hvis ikke det bliver brugt
    | dotNotation ASSIGN expression      #dotNotationAssignment
    ;

dotNotation
    : IDENTIFIER DOT IDENTIFIER                                     #simpleDot
    | IDENTIFIER LSQUARE arrayBodyInt RSQUARE DOT IDENTIFIER        #arrayDot
    ;

arrayBodyInt
    : expression (COMMA arrayBodyInt)? // var tidligere: INT_LITERAL (COMMA arrayBodyInt)?
    ;


expression
    : constant                                  #constantExpression
    | IDENTIFIER                                #identifierExpression
    | LPAREN expression RPAREN                  #parenthesisExpression
    | dotNotation                               #dotExpression
    | IDENTIFIER LSQUARE arrayBodyInt RSQUARE   #arrayAccessExpression
    | expression addOp expression               #additionExpression
    | expression compOp expression              #compareExpression
    | expression locOp expression               #logicalExpression
    ;


addOp: ADD | SUB;
compOp: GT | LT | GE | LE | EQUAL | NOTEQUAL;
locOp: AND | OR;

constant: INT_LITERAL | BOOLEAN_LITERAL;


/*
 * Lexer Rules
*/
 // TODO: Muligvis tilføj NULL. Hvis det tilføjes, gør det inde i rapporten ligeså.


INT_LITERAL: Digits+;
BOOLEAN_LITERAL: 'true' | 'false';
//NULL_LITERAL: 'null';

//Keyword
IF              : 'if';
INT             : 'int';
BOOLEAN         : 'boolean';
INITIALSTATE    : 'initialState';
GOALSTATE       : 'goalState';
OBJECTS         : 'objects';
TYPE            : 'type';
ACTION          : 'action';
IMPORT          : 'import';
DEFINE          : 'define';
DOMAIN          : 'domain';
PROBLEM         : 'problem';


// Separators
LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
LSQUARE : '[';
RSQUARE : ']';
SEMI   : ';';
COMMA  : ',';
DOT    : '.';

// Operators
ASSIGN   : '=';
GT       : '>';
LT       : '<';
COLON    : ':';
EQUAL    : '==';
LE       : '<=';
GE       : '>=';
NOTEQUAL : '!=';
AND      : '&&';
OR       : '||';
ADD      : '+';
SUB      : '-';


WS: [ \t\r\n]+ -> skip;

IDENTIFIER: Letter LetterOrDigit*;

fragment Digits: [0-9];

fragment Letter: [a-zA-Z];

fragment LetterOrDigit: Letter | Digits;
