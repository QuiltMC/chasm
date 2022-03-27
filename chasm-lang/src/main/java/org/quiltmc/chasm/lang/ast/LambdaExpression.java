package org.quiltmc.chasm.lang.ast;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.Scope;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.FunctionExpression;

public class LambdaExpression extends AbstractExpression implements FunctionExpression {
    private final String parameter;
    private final Expression body;
    private final ScopeStack scope;

    public LambdaExpression(ParseTree tree, String parameter, Expression body, ScopeStack scope) {
        super(tree);
        this.parameter = parameter;
        this.body = body;
        this.scope = scope;
    }

    @Override
    public Expression call(Expression argument) {
        scope.push(Scope.singleton(parameter, argument));
        Expression result = body.resolve(scope);
        scope.pop();
        return result;
    }

    @Override
    public Expression resolve(ScopeStack scope) {
        return new LambdaExpression(getParseTree(), parameter, body, scope.copy());
    }

    @Override
    public Expression reduce(Cache cache) {
        return this;
    }
}
