grammar Chasm;

@header {
    package org.quiltmc.chasm.lang.antlr;
}

file
    : map EOF
    ;

expression
    : IDENTIFIER # ReferenceExpression
    | expression '.' IDENTIFIER # MemberExpression
    | expression '.' '[' expression ']' # IndexExpression
    | expression '.' '<' expression '>' # FilterExpression
    | expression '(' expression ')' # CallExpression
    | map # MapExpression
    | list # ListExpression
    | literal # LiteralExpression
    | '(' expression ')' # GroupExpression
    | expression op=('*' | '/') expression # BinaryExpression
    | expression op=('+' | '-') expression # BinaryExpression
    | expression op=('<' | '<=' | '=' | '>=' | '>') expression # BinaryExpression
    | <assoc=right> expression '?' expression ':' expression # TernaryExpression
    | IDENTIFIER '->' expression # LambdaExpression
    ;

literal
    : STRING # StringExpression
    | INTEGER # IntegerExpression
    | BOOLEAN # BooleanExpression
    | NONE # NoneExpression
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

WHITESPACE: (' '  | '\n' | '\r' | '\t') -> skip;

OPERATOR: '+' | '-' | '*' | '/';

BOOLEAN: 'true' | 'false';
NONE: 'none';

STRING: '"' .*? '"';
INTEGER: [+-]? [0-9]+;
IDENTIFIER: [_$a-zA-Z] [_a-zA-Z0-9]*;
