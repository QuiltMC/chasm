package org.quiltmc.chasm.lang.ast;

import org.quiltmc.chasm.lang.ReductionContext;

public class TernaryExpression implements Expression {
    private final Expression condition;
    private final Expression trueExpression;
    private final Expression falseExpression;

    public TernaryExpression(Expression condition, Expression trueExpression, Expression falseExpression) {
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    @Override
    public void resolve(String identifier, Expression value) {
        condition.resolve(identifier, value);
        trueExpression.resolve(identifier, value);
        falseExpression.resolve(identifier, value);
    }

    @Override
    public Expression reduce(ReductionContext context) {
        Expression condition = context.reduce(this.condition);

        if (condition instanceof ConstantBooleanExpression) {
            if (((ConstantBooleanExpression) condition).getValue()) {
                return context.reduce(trueExpression);
            } else {
                return context.reduce(falseExpression);
            }
        }

        throw new RuntimeException("Condition in ternary must be a boolean.");
    }

    @Override
    public TernaryExpression copy() {
        return new TernaryExpression(condition.copy(), trueExpression.copy(), falseExpression.copy());
    }
}
