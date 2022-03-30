package org.quiltmc.chasm.lang.ast;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.op.Expression;

public class TernaryExpression extends AbstractExpression {
    private final Expression condition;
    private final Expression trueExpression;
    private final Expression falseExpression;

    public TernaryExpression(ParseTree tree,
                              Expression condition, Expression trueExpression, Expression falseExpression) {
        super(tree);
        this.condition = condition;
        this.trueExpression = trueExpression;
        this.falseExpression = falseExpression;
    }

    @Override
    public Expression resolve(ScopeStack scope) {
        return new TernaryExpression(getParseTree(), condition.resolve(scope), trueExpression.resolve(scope),
                falseExpression.resolve(
                        scope));
    }

    @Override
    public Expression reduce(Cache cache) {
        Expression condition = cache.reduceCached(this.condition);

        if (condition instanceof ConstantBooleanExpression) {
            if (((ConstantBooleanExpression) condition).getValue()) {
                return cache.reduceCached(trueExpression);
            } else {
                return cache.reduceCached(falseExpression);
            }
        }

        throw new RuntimeException("Condition in ternary must be a boolean.");
    }
}
