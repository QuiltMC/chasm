grammar Chasm;

@header {
    package org.quiltmc.chasm.lang.antlr;
}

file
    : expression EOF
    ;

expression
    : ('$'? IDENTIFIER) # ReferenceExpression
    | expression '.' IDENTIFIER # MemberExpression
    | expression '[' expression ']' # IndexExpression
    | expression '(' expression ')' # CallExpression
    | map # MapExpression
    | list # ListExpression
    | literal # LiteralExpression
    | '(' expression ')' # GroupExpression
    | op=(PLUS | MINUS | NOT | INVERT) expression # UnaryExpression
    | expression op=(MULTIPLY | DIVIDE | MODULO) expression # BinaryExpression
    | expression op=(PLUS | MINUS) expression # BinaryExpression
    | expression op=(SHIFT_LEFT | SHIFT_RIGHT | UNSIGNED_SHIFT_RIGHT) expression # BinaryExpression
    | expression op=(LESS_THAN | LESS_THAN_EQUAL | GREATER_THAN | GREATER_THAN_EQUAL) expression # BinaryExpression
    | expression op=(EQUAL | NOT_EQUAL) expression # BinaryExpression
    | expression op=BITWISE_AND expression # BinaryExpression
    | expression op=BITWISE_XOR expression # BinaryExpression
    | expression op=BITWISE_OR expression # BinaryExpression
    | expression op=LOGICAL_AND expression # BinaryBooleanExpression
    | expression op=LOGICAL_OR expression # BinaryBooleanExpression
    | <assoc=right> expression '?' expression ':' expression # TernaryExpression
    | IDENTIFIER '->' expression # LambdaExpression
    ;

literal
    : NULL # NullLiteral
    | BOOLEAN # BooleanLiteral
    | INTEGER # IntegerLiteral
   // | FLOAT # FloatLiteral
    | STRING # StringLiteral
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

NULL: 'null';
BOOLEAN: 'true' | 'false';
INTEGER: [+-]? [0-9]+ | '0x' [0-9a-fA-F]+ | '0b' [0-1]+;
//FLOAT: [+-]? [0-9]+ '.' [0-9]+ ('e' [+-]? [0-9]+)?;
STRING: '"' .*? '"';

IDENTIFIER: [_a-zA-Z] [_a-zA-Z0-9]*;

// Operators
PLUS: '+';
MINUS: '-';
NOT: '!';
INVERT: '~';
MULTIPLY: '*';
DIVIDE: '/';
MODULO: '%';
SHIFT_LEFT: '<<';
SHIFT_RIGHT: '>>';
UNSIGNED_SHIFT_RIGHT: '>>>';
LESS_THAN: '<';
LESS_THAN_EQUAL: '<=';
GREATER_THAN: '>';
GREATER_THAN_EQUAL: '>=';
EQUAL: '=';
NOT_EQUAL: '!=';
BITWISE_AND: '&';
BITWISE_XOR: '^';
BITWISE_OR: '|';
LOGICAL_AND: '&&';
LOGICAL_OR: '||';
