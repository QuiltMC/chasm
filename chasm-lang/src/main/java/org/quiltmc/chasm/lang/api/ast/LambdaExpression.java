package org.quiltmc.chasm.lang.api.ast;

import org.quiltmc.chasm.lang.internal.render.RendererConfig;

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
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append(identifier);
        builder.append(" -> ");
        inner.render(config, builder, currentIndentationMultiplier);
    }

    @Override
    public LambdaExpression copy() {
        return new LambdaExpression(identifier, inner.copy());
    }
}
