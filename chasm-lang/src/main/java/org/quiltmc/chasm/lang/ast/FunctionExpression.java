package org.quiltmc.chasm.lang.ast;

import java.util.HashMap;
import java.util.Map;

import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.op.Callable;

public class FunctionExpression implements Expression, Callable {
    private final Map<Expression, Expression> results = new HashMap<>();
    private final String parameter;
    private final Expression body;

    public FunctionExpression(String parameter, Expression body) {
        this.parameter = parameter;
        this.body = body;
    }

    @Override
    public void resolve(String identifier, Expression value) {
        if (!parameter.equals(identifier)) {
            body.resolve(identifier, value);
        }
    }

    @Override
    public Expression reduce(ReductionContext context) {
        return this;
    }

    @Override
    public FunctionExpression copy() {
        return this;
    }

    @Override
    public boolean canCall(Expression expression) {
        return true;
    }

    @Override
    public Expression call(Expression argument) {
        Expression result = results.get(argument);
        if (result == null) {
            Expression body = this.body.copy();
            body.resolve(parameter, argument);
            results.put(argument, body);
            return body;
        }
        return result;
    }
}
