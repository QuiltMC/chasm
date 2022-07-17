package org.quiltmc.chasm.lang.api.ast;

import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class UnaryExpression extends Expression {
    private Expression inner;
    private Operator operator;

    public UnaryExpression(Expression inner, Operator operator) {
        this.inner = inner;
        this.operator = operator;
    }

    public Expression getInner() {
        return inner;
    }

    public void setInner(Expression inner) {
        this.inner = inner;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append(operator.image);
        inner.render(config, builder, currentIndentationMultiplier);
    }

    @Override
    public UnaryExpression copy() {
        return new UnaryExpression(inner.copy(), operator);
    }

    public enum Operator {
        PLUS("+"),
        MINUS("-"),
        NOT("!"),
        INVERT("~");

        private final String image;

        Operator(String image) {
            this.image = image;
        }

        public String getImage() {
            return image;
        }

        @Override
        public String toString() {
            return image;
        }
    }
}
