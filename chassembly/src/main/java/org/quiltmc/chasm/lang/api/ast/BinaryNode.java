package org.quiltmc.chasm.lang.api.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * A binary expression, e.g. {@code foo + bar}.
 */
public class BinaryNode extends Node {
    private Node left;
    private Operator operator;
    private Node right;

    /**
     * Constructs a binary expression.
     *
     * @see Ast#binary(Node, Operator, Node)
     */
    public BinaryNode(Node left, Operator operator, Node right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    /**
     * Gets the left hand side of this binary expression.
     */
    public Node getLeft() {
        return left;
    }

    /**
     * Sets the left hand side of this binary expression.
     */
    public void setLeft(Node left) {
        this.left = left;
    }

    /**
     * Gets the right hand side of this binary expression.
     */
    public Node getRight() {
        return right;
    }

    /**
     * Sets the right hand side of this binary expression.
     */
    public void setRight(Node right) {
        this.right = right;
    }

    /**
     * Gets the operator of this binary expression.
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Sets the operator of this binary expression.
     */
    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
        left.resolve(resolver);
        right.resolve(resolver);
    }

    @Override
    public Node evaluate(Evaluator evaluator) {
        Node left = this.left.evaluate(evaluator);

        // Short-circuiting operators:
        if (operator == Operator.BOOLEAN_AND || operator == Operator.BOOLEAN_OR) {
            if (!(left instanceof BooleanNode)) {
                throw new EvaluationException("The left side of boolean operator " + operator
                        + " must be a boolean but found " + left);
            }

            if (operator == BinaryNode.Operator.BOOLEAN_OR && ((BooleanNode) left).getValue()) {
                return BooleanNode.TRUE;
            }

            if (operator == BinaryNode.Operator.BOOLEAN_AND && !((BooleanNode) left).getValue()) {
                return BooleanNode.FALSE;
            }
        }

        Node right = this.right.evaluate(evaluator);

        switch (operator) {
            case PLUS: {
                if (left instanceof MapNode && right instanceof MapNode) {
                    Map<String, Node> leftEntries = ((MapNode) left).getEntries();
                    Map<String, Node> rightEntries = ((MapNode) right).getEntries();

                    Map<String, Node> newEntries = new LinkedHashMap<>(leftEntries);
                    newEntries.putAll(rightEntries);

                    return new MapNode(newEntries);
                }

                if (left instanceof ListNode && right instanceof ListNode) {
                    List<Node> leftEntries = ((ListNode) left).getEntries();
                    List<Node> rightEntries = ((ListNode) right).getEntries();

                    ArrayList<Node> newEntries = new ArrayList<>(leftEntries);
                    newEntries.addAll(rightEntries);

                    return new ListNode(newEntries);
                }

                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() + ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return Ast.literal(((FloatNode) left).getValue() + ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return Ast.literal(((IntegerNode) left).getValue() + ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return Ast.literal(((FloatNode) left).getValue() + ((IntegerNode) right).getValue());
                }

                if (left instanceof StringNode && right instanceof ValueNode) {
                    return Ast.literal(((StringNode) left).getValue() + ((ValueNode<?>) right).getValue());
                }

                if (left instanceof ValueNode && right instanceof StringNode) {
                    return Ast.literal(((ValueNode<?>) left).getValue() + ((StringNode) right).getValue());
                }
            }
            break;
            case MINUS: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() - ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return Ast.literal(((FloatNode) left).getValue() - ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return Ast.literal(((IntegerNode) left).getValue() - ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return Ast.literal(((FloatNode) left).getValue() - ((IntegerNode) right).getValue());
                }
            }
            break;
            case MULTIPLY: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() * ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return Ast.literal(((FloatNode) left).getValue() * ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return Ast.literal(((IntegerNode) left).getValue() * ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return Ast.literal(((FloatNode) left).getValue() * ((IntegerNode) right).getValue());
                }
            }
            break;
            case DIVIDE: {

                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() / ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return Ast.literal(((FloatNode) left).getValue() / ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return Ast.literal(((IntegerNode) left).getValue() / ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return Ast.literal(((FloatNode) left).getValue() / ((IntegerNode) right).getValue());
                }
            }
            break;
            case MODULO: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() % ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return Ast.literal(((FloatNode) left).getValue() % ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return Ast.literal(((IntegerNode) left).getValue() % ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return Ast.literal(((FloatNode) left).getValue() % ((IntegerNode) right).getValue());
                }
            }
            break;
            case SHIFT_LEFT: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() << ((IntegerNode) right).getValue());
                }
            }
            break;
            case SHIFT_RIGHT: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() >> ((IntegerNode) right).getValue());
                }
            }
            break;
            case SHIFT_RIGHT_UNSIGNED: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() >>> ((IntegerNode) right).getValue());
                }
            }
            break;
            case LESS_THAN: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() < ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return Ast.literal(((FloatNode) left).getValue() < ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return Ast.literal(((IntegerNode) left).getValue() < ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return Ast.literal(((FloatNode) left).getValue() < ((IntegerNode) right).getValue());
                }
            }
            break;
            case LESS_THAN_OR_EQUAL: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() <= ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return Ast.literal(((FloatNode) left).getValue() <= ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return Ast.literal(((IntegerNode) left).getValue() <= ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return Ast.literal(((FloatNode) left).getValue() <= ((IntegerNode) right).getValue());
                }
            }
            break;
            case GREATER_THAN: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() > ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return Ast.literal(((FloatNode) left).getValue() > ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return Ast.literal(((IntegerNode) left).getValue() > ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return Ast.literal(((FloatNode) left).getValue() > ((IntegerNode) right).getValue());
                }
            }
            break;
            case GREATER_THAN_OR_EQUAL: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() >= ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return Ast.literal(((FloatNode) left).getValue() >= ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return Ast.literal(((IntegerNode) left).getValue() >= ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return Ast.literal(((FloatNode) left).getValue() >= ((IntegerNode) right).getValue());
                }
            }
            break;
            case EQUAL: {
                if (left instanceof NullNode) {
                    return Ast.literal(right instanceof NullNode);
                }
                if (right instanceof NullNode) {
                    return BooleanNode.FALSE; // Left was checked before
                }

                if (left instanceof ValueNode && right instanceof ValueNode) {
                    return Ast.literal(
                            Objects.equals(((ValueNode<?>) left).getValue(), ((ValueNode<?>) right).getValue())
                    );
                }
            }
            break;
            case NOT_EQUAL: {
                if (left instanceof NullNode) {
                    return Ast.literal(!(right instanceof NullNode));
                }
                if (right instanceof NullNode) {
                    return BooleanNode.TRUE; // Left was checked before
                }

                if (left instanceof ValueNode && right instanceof ValueNode) {
                    return Ast.literal(
                            !Objects.equals(((ValueNode<?>) left).getValue(), ((ValueNode<?>) right).getValue())
                    );
                }
            }
            break;
            case BOOLEAN_AND: {
                // Left side was already checked before
                if (right instanceof BooleanNode) {
                    return right;
                }
            }
            break;
            case BOOLEAN_OR: {
                // Left side was already checked before
                if (right instanceof BooleanNode) {
                    return right;
                }
            }
            break;
            case BITWISE_AND: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() & ((IntegerNode) right).getValue());
                }
            }
            break;
            case BITWISE_XOR: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() ^ ((IntegerNode) right).getValue());
                }
            }
            break;
            case BITWISE_OR: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return Ast.literal(((IntegerNode) left).getValue() | ((IntegerNode) right).getValue());
                }
            }
            break;
            default: {
                throw new EvaluationException("Unexpected operator " + operator);
            }
        }

        throw new EvaluationException(
                "Can't apply binary operator " + operator + " to " + left + " and " + right
        );
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        final boolean leftNeedsBrackets = left instanceof BinaryNode && (
                ((BinaryNode) left).operator == operator && operator.requiresBracketsWithSelf
                        || ((BinaryNode) left).operator.morePrecedenceThan(operator.precedence)
        )
                || left instanceof UnaryNode && ((UnaryNode) left).getOperator().morePrecedenceThan(operator.precedence)
                || left instanceof TernaryNode;
        final boolean rightNeedsBrackets = right instanceof BinaryNode && (
                ((BinaryNode) right).operator == operator && operator.requiresBracketsWithSelf
                        || ((BinaryNode) right).operator.morePrecedenceThan(operator.precedence)
        )
                || right instanceof UnaryNode && ((UnaryNode) right).getOperator()
                .morePrecedenceThan(operator.precedence)
                || right instanceof TernaryNode;

        if (leftNeedsBrackets) {
            builder.append('(');
        }
        left.render(renderer, builder, currentIndentationMultiplier);
        if (leftNeedsBrackets) {
            builder.append(')');
        }

        builder.append(' ').append(operator.image).append(' ');

        if (rightNeedsBrackets) {
            builder.append('(');
        }
        right.render(renderer, builder, currentIndentationMultiplier);
        if (rightNeedsBrackets) {
            builder.append(')');
        }
    }

    /**
     * The operator of a binary expression.
     */
    public enum Operator {
        /**
         * The addition operator, {@code +}.
         */
        PLUS("+", 4, false),
        /**
         * The subtraction operator, {@code -}.
         */
        MINUS("-", 4, true),
        /**
         * The multiplication operator, {@code *}.
         */
        MULTIPLY("*", 3, false),
        /**
         * The division operator, {@code /}.
         */
        DIVIDE("/", 3, true),
        /**
         * The modulo operator, {@code %}.
         */
        MODULO("%", 3, false),
        /**
         * The left shift operator, {@code <<}.
         */
        SHIFT_LEFT("<<", 5, false),
        /**
         * The arithmetic right shift operator, {@code >>}.
         */
        SHIFT_RIGHT(">>", 5, false),
        /**
         * The unsigned right shift operator, {@code >>>}.
         */
        SHIFT_RIGHT_UNSIGNED(">>>", 5, false),
        /**
         * The less than operator, {@code <}.
         */
        LESS_THAN("<", 6, false),
        /**
         * The less than or equal operator, {@code <=}.
         */
        LESS_THAN_OR_EQUAL("<=", 6, false),
        /**
         * The greater than operator, {@code >}.
         */
        GREATER_THAN(">", 6, false),
        /**
         * The greater than or equal operator, {@code >=}.
         */
        GREATER_THAN_OR_EQUAL(">=", 6, false),
        /**
         * The equality operator, {@code =}.
         */
        EQUAL("=", 7, false),
        /**
         * The inequality operator, {@code !=}.
         */
        NOT_EQUAL("!=", 7, false),
        /**
         * The bitwise and operator, {@code &}.
         */
        BITWISE_AND("&", 8, false),
        /**
         * The bitwise xor operator, {@code ^}.
         */
        BITWISE_XOR("^", 9, false),
        /**
         * The bitwise or operator, {@code |}.
         */
        BITWISE_OR("|", 10, false),
        /**
         * The boolean and operator, {@code &&}.
         */
        BOOLEAN_AND("&&", 11, false),
        /**
         * The boolean or operator, {@code ||}.
         */
        BOOLEAN_OR("||", 12, false);

        private final String image;
        private final int precedence;
        private final boolean requiresBracketsWithSelf;

        private static final Map<String, Operator> IMAGE_TO_OPERATOR = new HashMap<>();
        static {
            for (Operator op : values()) {
                IMAGE_TO_OPERATOR.put(op.image, op);
            }
        }

        Operator(String image, int precedence, boolean requiresBracketsWithSelf) {
            this.image = image;
            this.precedence = precedence;
            this.requiresBracketsWithSelf = requiresBracketsWithSelf;
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

        public boolean requiresBracketsWithSelf() {
            return requiresBracketsWithSelf;
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
