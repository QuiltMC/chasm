package org.quiltmc.chasm.lang.api.ast;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * A unary operator expression, e.g. {@code -foo}.
 */
public class UnaryNode extends Node {
    private Node inner;
    private Operator operator;

    /**
     * Creates a unary operator expression.
     *
     * @see Ast#unary(Operator, Node)
     */
    public UnaryNode(Node inner, Operator operator) {
        this.inner = inner;
        this.operator = operator;
    }

    /**
     * Gets the operand.
     */
    public Node getInner() {
        return inner;
    }

    /**
     * Sets the operand.
     */
    public void setInner(Node inner) {
        this.inner = inner;
    }

    /**
     * Gets the operator.
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Sets the operator.
     */
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append(operator.image);
        boolean wrapWithBraces = inner instanceof BinaryNode && ((BinaryNode) inner).getOperator()
                .morePrecedenceThan(operator.precedence)
                // we don't have to do the funky requiresBracketsWithSelf here luckily
                || inner instanceof UnaryNode && ((UnaryNode) inner).operator.morePrecedenceThan(operator.precedence)
                || inner instanceof TernaryNode;

        if (wrapWithBraces) {
            builder.append('(');
        }
        inner.render(renderer, builder, currentIndentationMultiplier);
        if (wrapWithBraces) {
            builder.append(')');
        }
    }

    @Override
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
        inner.resolve(resolver);
    }

    @Override
    @ApiStatus.OverrideOnly
    public Node evaluate(Evaluator evaluator) {
        Node inner = this.inner.evaluate(evaluator);

        switch (operator) {
            case PLUS: {
                if (inner instanceof IntegerNode) {
                    return inner;
                }

                if (inner instanceof FloatNode) {
                    return inner;
                }
            }
            break;
            case MINUS: {
                if (inner instanceof IntegerNode) {
                    return new IntegerNode(-((IntegerNode) inner).getValue());
                }

                if (inner instanceof FloatNode) {
                    return new FloatNode(-((FloatNode) inner).getValue());
                }
            }
            break;
            case NOT: {
                if (inner instanceof BooleanNode) {
                    return BooleanNode.from(!((BooleanNode) inner).getValue());
                }
            }
            break;
            case INVERT: {
                if (inner instanceof IntegerNode) {
                    return new IntegerNode(~((IntegerNode) inner).getValue());
                }
            }
            break;
            default: {
                throw new EvaluationException(
                        "Unknown unary operator " + operator
                );
            }
        }

        throw new EvaluationException("Can't apply unary operator " + operator + " to " + inner);
    }

    /**
     * The operator of a unary expression.
     */
    public enum Operator {
        /**
         * The unary plus operator, {@code +}.
         */
        PLUS("+", 2),
        /**
         * The negation operator, {@code -}.
         */
        MINUS("-", 2),
        /**
         * The boolean not operator, {@code !}.
         */
        NOT("!", 2),
        /**
         * The bitwise inversion operator, {@code ~}.
         */
        INVERT("~", 2);

        private final String image;
        private final int precedence;

        private static final Map<String, Operator> IMAGE_TO_OPERATOR = new HashMap<>();
        static {
            for (Operator op : values()) {
                IMAGE_TO_OPERATOR.put(op.image, op);
            }
        }

        Operator(String image, int precedence) {
            this.image = image;
            this.precedence = precedence;
        }

        /**
         * The image of this operator, or the string that represents it in code.
         */
        public String getImage() {
            return image;
        }

        /**
         * The precedence of the operator, lower values means it's evaluated first by default.
         */
        public int getPrecedence() {
            return precedence;
        }

        @Override
        public String toString() {
            return image;
        }

        /**
         * Returns whether this operator's precedence is greater than the given value.
         */
        public boolean morePrecedenceThan(int precedence) {
            return this.precedence > precedence;
        }

        /**
         * Gets an operator by its image.
         */
        @Nullable
        public static Operator getOperator(String image) {
            return IMAGE_TO_OPERATOR.get(image);
        }
    }
}
