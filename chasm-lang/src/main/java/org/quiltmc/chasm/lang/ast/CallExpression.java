package org.quiltmc.chasm.lang.ast;

import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.op.Callable;

public class CallExpression implements Expression {
    private final Expression function;
    private final Expression argument;

    public CallExpression(Expression function, Expression argument) {
        this.function = function;
        this.argument = argument;
    }

    @Override
    public void resolve(String identifier, Expression value) {
        function.resolve(identifier, value);
        argument.resolve(identifier, value);
    }

    @Override
    public Expression reduce(ReductionContext context) {
        Expression function = context.reduce(this.function);
        Expression argument = context.reduce(this.argument);

        if (function instanceof Callable) {
            Expression result = ((Callable) function).call(argument);
            return context.reduce(result);
        }

        throw new RuntimeException("Can only call functions.");
    }

    @Override
    public CallExpression copy() {
        return new CallExpression(function.copy(), argument.copy());
    }
}
