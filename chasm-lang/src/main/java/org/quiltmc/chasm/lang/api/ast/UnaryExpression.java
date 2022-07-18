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
        boolean wrapWithBraces = inner instanceof BinaryExpression && ((BinaryExpression) inner).getOperator().morePrecedenceThan(operator.precedence)
                              || inner instanceof UnaryExpression && ((UnaryExpression) inner).operator.morePrecedenceThan(operator.precedence)
                              || inner instanceof TernaryExpression;

        if (wrapWithBraces) {
            builder.append('(');
        }
        inner.render(config, builder, currentIndentationMultiplier);
        if (wrapWithBraces) {
            builder.append(')');
        }
    }

    @Override
    public UnaryExpression copy() {
        return new UnaryExpression(inner.copy(), operator);
    }

    public enum Operator {
        PLUS("+", 2),
        MINUS("-", 2),
        NOT("!", 2),
        INVERT("~", 2);

        private final String image;
        private final int precedence;

        Operator(String image, int precedence) {
            this.image = image;
            this.precedence = precedence;
        }

        public String getImage() {
            return image;
        }

        public int getPrecedence() {
            return precedence;
        }

        @Override
        public String toString() {
            return image;
        }

        public boolean morePrecedenceThan(int priority) {
            return this.precedence > priority;
        }
    }
}
