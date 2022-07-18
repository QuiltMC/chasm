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
        boolean leftNeedsBrackets = left instanceof BinaryExpression && ((BinaryExpression) left).operator.morePrecedenceThan(operator.precedence)
                                 || left instanceof UnaryExpression && ((UnaryExpression) left).getOperator().morePrecedenceThan(operator.precedence)
                                 || left instanceof TernaryExpression;
        boolean rightNeedsBrackets = right instanceof BinaryExpression && ((BinaryExpression) right).operator.morePrecedenceThan(operator.precedence)
                                  || right instanceof UnaryExpression && ((UnaryExpression) right).getOperator().morePrecedenceThan(operator.precedence)
                                  || right instanceof TernaryExpression;

        if (leftNeedsBrackets) {
            builder.append('(');
        }
        left.render(config, builder, currentIndentationMultiplier);
        if (leftNeedsBrackets) {
            builder.append(')');
        }

        builder.append(' ').append(operator.image).append(' ');

        if (rightNeedsBrackets) {
            builder.append('(');
        }
        right.render(config, builder, currentIndentationMultiplier);
        if (rightNeedsBrackets) {
            builder.append(')');
        }
    }

    public enum Operator {
        PLUS("+", 4),
        MINUS("-", 4),
        MULTIPLY("*", 3),
        DIVIDE("/", 3),
        MODULO("%", 3),
        SHIFT_LEFT("<<", 5),
        SHIFT_RIGHT(">>", 5),
        SHIFT_RIGHT_UNSIGNED(">>>", 5),
        LESS_THAN("<", 6),
        LESS_THAN_OR_EQUAL("<=", 6),
        GREATER_THAN(">", 6),
        GREATER_THAN_OR_EQUAL(">=", 6),
        EQUAL("==", 7),
        NOT_EQUAL("!=", 7),
        BITWISE_AND("&", 8),
        BITWISE_XOR("^", 9),
        BITWISE_OR("|", 10),
        BOOLEAN_AND("&&", 11),
        BOOLEAN_OR("||", 12);

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
