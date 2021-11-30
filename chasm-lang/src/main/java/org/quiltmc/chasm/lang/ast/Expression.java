package org.quiltmc.chasm.lang.ast;

import org.quiltmc.chasm.lang.ReductionContext;

public interface Expression {
    static NoneExpression none() {
        return NoneExpression.NONE;
    }

    void resolve(String identifier, Expression value);

    Expression reduce(ReductionContext context);

    Expression copy();
}
