package org.quiltmc.chasm.lang.ast;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.FunctionExpression;

public class CallExpression extends AbstractExpression {
    private final Expression function;
    private final Expression argument;

    public CallExpression(ParseTree tree, Expression function, Expression argument) {
        super(tree);
        this.function = function;
        this.argument = argument;
    }

    @Override
    public Expression resolve(ScopeStack scope) {
        return new CallExpression(getParseTree(), function.resolve(scope), argument.resolve(scope));
    }

    @Override
    public Expression reduce(Cache cache) {
        Expression function = cache.reduceCached(this.function);
        Expression argument = cache.reduceCached(this.argument);

        if (function instanceof FunctionExpression) {
            FunctionExpression functionExpression = (FunctionExpression) function;
            Expression result = cache.callCached(functionExpression, argument);
            return cache.reduceCached(result);
        } else {
            // TODO: Proper Error
            throw new RuntimeException("Can only call functions.");
        }
    }
}
