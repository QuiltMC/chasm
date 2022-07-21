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
        boolean leftNeedsBrackets = left instanceof BinaryExpression && (
                                    ((BinaryExpression) left).operator == operator && operator.requiresBracketsWithSelf
                                    || ((BinaryExpression) left).operator.morePrecedenceThan(operator.precedence)
                                 )
                                 || left instanceof UnaryExpression && ((UnaryExpression) left).getOperator().morePrecedenceThan(operator.precedence)
                                 || left instanceof TernaryExpression;
        boolean rightNeedsBrackets = right instanceof BinaryExpression && (
                                     ((BinaryExpression) right).operator == operator && operator.requiresBracketsWithSelf
                                     || ((BinaryExpression) right).operator.morePrecedenceThan(operator.precedence)
                                  )
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
        PLUS("+", 4, false),
        MINUS("-", 4, true),
        MULTIPLY("*", 3, false),
        DIVIDE("/", 3, true),
        MODULO("%", 3, false),
        SHIFT_LEFT("<<", 5, false),
        SHIFT_RIGHT(">>", 5, false),
        SHIFT_RIGHT_UNSIGNED(">>>", 5, false),
        LESS_THAN("<", 6, false),
        LESS_THAN_OR_EQUAL("<=", 6, false),
        GREATER_THAN(">", 6, false),
        GREATER_THAN_OR_EQUAL(">=", 6, false),
        EQUAL("==", 7, false),
        NOT_EQUAL("!=", 7, false),
        BITWISE_AND("&", 8, false),
        BITWISE_XOR("^", 9, false),
        BITWISE_OR("|", 10, false),
        BOOLEAN_AND("&&", 11, false),
        BOOLEAN_OR("||", 12, false);

        private final String image;
        private final int precedence;
        private final boolean requiresBracketsWithSelf;

        Operator(String image, int precedence, boolean requiresBracketsWithSelf) {
            this.image = image;
            this.precedence = precedence;
            this.requiresBracketsWithSelf = requiresBracketsWithSelf;
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

        public boolean morePrecedenceThan(int precedence) {
            return this.precedence > precedence;
        }

        public boolean requiresBracketsWithSelf() {
            return requiresBracketsWithSelf;
        }
    }
}
