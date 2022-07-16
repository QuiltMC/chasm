package org.quiltmc.chasm.lang.api.ast;

import java.util.Objects;

public class LambdaExpression extends Expression {
    private String identifier;
    private Expression inner;

    public LambdaExpression(String identifier, Expression expression) {
        this.identifier = identifier;
        this.inner = expression;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Expression getInner() {
        return inner;
    }

    public void setInner(Expression inner) {
        this.inner = inner;
    }

    @Override
    public LambdaExpression copy() {
        return new LambdaExpression(identifier, inner.copy());
    }
}
