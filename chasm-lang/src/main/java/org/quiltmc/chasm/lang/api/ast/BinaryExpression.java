package org.quiltmc.chasm.lang.api.ast;

import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class BinaryExpression extends Expression {
    private Expression left;
    private Operator operator;
    private Expression right;

    public BinaryExpression(Expression left, Operator operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public void setLeft(Expression left) {
        this.left = left;
    }

    public Expression getRight() {
        return right;
    }

    public void setRight(Expression right) {
        this.right = right;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public BinaryExpression copy() {
        return new BinaryExpression(left.copy(), operator, right.copy());
    }

    @Override
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        left.render(config, builder, currentIndentationMultiplier);
        builder.append(' ').append(operator.image).append(' ');
        right.render(config, builder, currentIndentationMultiplier);
    }

    public enum Operator {
        PLUS("+"),
        MINUS("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        MODULO("%"),
        SHIFT_LEFT("<<"),
        SHIFT_RIGHT(">>"),
        SHIFT_RIGHT_UNSIGNED(">>>"),
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL("<="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL(">="),
        EQUAL("="),
        NOT_EQUAL("!="),
        BITWISE_AND("&"),
        BITWISE_XOR("^"),
        BITWISE_OR("|"),
        BOOLEAN_AND("&&"),
        BOOLEAN_OR("||");

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
