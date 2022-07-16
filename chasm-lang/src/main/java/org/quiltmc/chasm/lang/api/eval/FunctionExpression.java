package org.quiltmc.chasm.lang.api.eval;

import org.quiltmc.chasm.lang.api.ast.Expression;

public abstract class FunctionExpression extends Expression {
    public abstract Expression apply(Evaluator evaluator, Expression expression);

    @Override
    public Expression copy() {
        return this;
    }
}
