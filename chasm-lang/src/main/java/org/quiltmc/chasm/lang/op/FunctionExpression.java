package org.quiltmc.chasm.lang.op;

public interface FunctionExpression extends Expression {
    Expression call(Expression argument);
}
