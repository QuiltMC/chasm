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
    | op=('+' | '-' | '!' | '~') expression # UnaryExpression
    | expression op=('*' | '/' | '%') expression # BinaryExpression
    | expression op=('+' | '-') expression # BinaryExpression
    | expression op=('<<' | '>>' | '>>>') expression # BinaryExpression
    | expression op=('<' | '<=' | '>=' | '>') expression # BinaryExpression
    | expression op=('=' | '!=') expression # BinaryExpression
    | expression op='&' expression # BinaryExpression
    | expression op='^' expression # BinaryExpression
    | expression op='|' expression # BinaryExpression
    | expression op='&&' expression # BinaryBooleanExpression
    | expression op='||' expression # BinaryBooleanExpression
    | <assoc=right> expression '?' expression ':' expression # TernaryExpression
    | IDENTIFIER '->' expression # LambdaExpression
    ;

literal
    : STRING # StringLiteral
    | TYPE # TypeLiteral
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

WHITESPACE: (' '  | '\n' | '\r' | '\t') -> skip;

OPERATOR: '+' | '-' | '*' | '/';

BOOLEAN: 'true' | 'false';
NONE: 'none';

TYPE: 'T"' .*? '"';
STRING: '"' .*? '"';
INTEGER: [+-]? [0-9]+;
IDENTIFIER: [_$a-zA-Z] [_a-zA-Z0-9]*;
