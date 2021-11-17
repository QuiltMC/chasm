grammar Chasm;

@header {
    package org.quiltmc.chasm.lang;
}

expression
    : IDENTIFIER # Reference
    | expression '.' IDENTIFIER # MemberAccess
    | expression '<' expression '>' # FilterOperation
    | expression '(' expression ')' # FunctionCall
    | map # MapConstructor
    | list # ListConstructor
    | literal # LiteralValue
    | '(' expression ')' # Grouping
    | expression ('*' | '/') expression # MultiplyDivide
    | expression ('+' | '-') expression # AddSubtract
    | expression ('<' | '=' | '>') expression # Compare
    | expression '?' expression ':' expression # Ternary
    | IDENTIFIER '->' expression # LambdaDefinition
    ;

literal
    : STRING # StringLiteral
    | INTEGER # IntegerLiteral
    | BOOLEAN # BooleanLiteral
    | NONE # NoneLiteral
    ;

map
    : '{' (mapEntry (',' mapEntry)* ','? )? '}'
    ;

mapEntry
    : IDENTIFIER ':' expression
    ;

list
    : '[' (expression (',' expression)* ','? )? ']'
    ;

WHITESPACE: (' '  | '\n' | '\r' | '\t' | EOF) -> skip;

OPERATOR: '+' | '-' | '*' | '/';

BOOLEAN: 'true' | 'false';
NONE: 'none';

STRING: '"' ~('"')* '"';
INTEGER: [+-]? [0-9]+;
IDENTIFIER: [_a-zA-Z] [_a-zA-Z0-9]*;
