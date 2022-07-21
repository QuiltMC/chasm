package org.quiltmc.chasm.lang.api.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public class BinaryNode extends Node {
    private Node left;
    private Operator operator;
    private Node right;

    public BinaryNode(Node left, Operator operator, Node right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public BinaryNode copy() {
        return new BinaryNode(left.copy(), operator, right.copy());
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
        Object leftValue = left instanceof LiteralNode ? ((LiteralNode) left).getValue() : null;

        // Short-circuiting operators:
        if (operator == Operator.BOOLEAN_AND || operator == Operator.BOOLEAN_OR) {
            if (!(leftValue instanceof Boolean)) {
                throw new EvaluationException("The left side of boolean operator " + operator
                        + " must be a boolean but found " + left);
            }

            if (operator == BinaryNode.Operator.BOOLEAN_OR && (Boolean) leftValue) {
                return left;
            }

            if (operator == BinaryNode.Operator.BOOLEAN_AND && !(Boolean) leftValue) {
                return left;
            }
        }

        Node right = this.right.evaluate(evaluator);
        Object rightValue = right instanceof LiteralNode ? ((LiteralNode) right).getValue() : null;

        switch (operator) {
            case PLUS: {
                if (left instanceof ListNode && right instanceof ListNode) {
                    List<Node> leftEntries = ((ListNode) left).getEntries();
                    List<Node> rightEntries = ((ListNode) right).getEntries();

                    ArrayList<Node> newEntries = new ArrayList<>(leftEntries);
                    newEntries.addAll(rightEntries);

                    return new ListNode(newEntries);
                }

                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue + (Long) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return new LiteralNode((Double) leftValue + (Double) rightValue);
                }

                if (leftValue instanceof Long && rightValue instanceof Double) {
                    return new LiteralNode((Long) leftValue + (Double) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Long) {
                    return new LiteralNode((Double) leftValue + (Long) rightValue);
                }

                if (leftValue instanceof String || rightValue instanceof String) {
                    return new LiteralNode(String.valueOf(leftValue) + rightValue);
                }
            } break;
            case MINUS: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue - (Long) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return new LiteralNode((Double) leftValue - (Double) rightValue);
                }

                if (leftValue instanceof Long && rightValue instanceof Double) {
                    return new LiteralNode((Long) leftValue - (Double) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Long) {
                    return new LiteralNode((Double) leftValue - (Long) rightValue);
                }
            } break;
            case MULTIPLY: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue * (Long) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return new LiteralNode((Double) leftValue * (Double) rightValue);
                }

                if (leftValue instanceof Long && rightValue instanceof Double) {
                    return new LiteralNode((Long) leftValue * (Double) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Long) {
                    return new LiteralNode((Double) leftValue * (Long) rightValue);
                }
            } break;
            case DIVIDE: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue / (Long) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return new LiteralNode((Double) leftValue / (Double) rightValue);
                }

                if (leftValue instanceof Long && rightValue instanceof Double) {
                    return new LiteralNode((Long) leftValue / (Double) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Long) {
                    return new LiteralNode((Double) leftValue / (Long) rightValue);
                }
            } break;
            case MODULO: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue % (Long) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return new LiteralNode((Double) leftValue % (Double) rightValue);
                }

                if (leftValue instanceof Long && rightValue instanceof Double) {
                    return new LiteralNode((Long) leftValue % (Double) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Long) {
                    return new LiteralNode((Double) leftValue % (Long) rightValue);
                }
            } break;
            case SHIFT_LEFT: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue << (Long) rightValue);
                }
            } break;
            case SHIFT_RIGHT: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue >> (Long) rightValue);
                }
            } break;
            case SHIFT_RIGHT_UNSIGNED: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue >>> (Long) rightValue);
                }
            } break;
            case LESS_THAN: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue < (Long) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return new LiteralNode((Double) leftValue < (Double) rightValue);
                }

                if (leftValue instanceof Long && rightValue instanceof Double) {
                    return new LiteralNode((Long) leftValue < (Double) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Long) {
                    return new LiteralNode((Double) leftValue < (Long) rightValue);
                }
            } break;
            case LESS_THAN_OR_EQUAL: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue <= (Long) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return new LiteralNode((Double) leftValue <= (Double) rightValue);
                }

                if (leftValue instanceof Long && rightValue instanceof Double) {
                    return new LiteralNode((Long) leftValue <= (Double) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Long) {
                    return new LiteralNode((Double) leftValue <= (Long) rightValue);
                }
            } break;
            case GREATER_THAN: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue > (Long) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return new LiteralNode((Double) leftValue > (Double) rightValue);
                }

                if (leftValue instanceof Long && rightValue instanceof Double) {
                    return new LiteralNode((Long) leftValue > (Double) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Long) {
                    return new LiteralNode((Double) leftValue > (Long) rightValue);
                }
            } break;
            case GREATER_THAN_OR_EQUAL: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue >= (Long) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Double) {
                    return new LiteralNode((Double) leftValue >= (Double) rightValue);
                }

                if (leftValue instanceof Long && rightValue instanceof Double) {
                    return new LiteralNode((Long) leftValue >= (Double) rightValue);
                }

                if (leftValue instanceof Double && rightValue instanceof Long) {
                    return new LiteralNode((Double) leftValue >= (Long) rightValue);
                }
            } break;
            case EQUAL: {
                if (left instanceof LiteralNode && right instanceof LiteralNode) {
                    return new LiteralNode(Objects.equals(leftValue, rightValue));
                }
            } break;
            case NOT_EQUAL: {
                if (left instanceof LiteralNode && right instanceof LiteralNode) {
                    return new LiteralNode(!Objects.equals(leftValue, rightValue));
                }
            } break;
            case BOOLEAN_AND: {
                // leftValue was already checked before
                if (rightValue instanceof Boolean) {
                    return right;
                }
            } break;
            case BOOLEAN_OR: {
                // leftValue was already checked before
                if (rightValue instanceof Boolean) {
                    return right;
                }
            } break;
            case BITWISE_AND: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue & (Long) rightValue);
                }
            } break;
            case BITWISE_XOR: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue ^ (Long) rightValue);
                }
            } break;
            case BITWISE_OR: {
                if (leftValue instanceof Long && rightValue instanceof Long) {
                    return new LiteralNode((Long) leftValue | (Long) rightValue);
                }
            } break;
            default: {
                throw new EvaluationException("Unexpected operator " + operator);
            }
        }

        throw new EvaluationException(
                "Can't apply operator " + operator + " to " + left + " and " + right
        );
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        boolean leftNeedsBrackets = left instanceof BinaryNode && (
                ((BinaryNode) left).operator == operator && operator.requiresBracketsWithSelf
                        || ((BinaryNode) left).operator.morePrecedenceThan(operator.precedence)
        )
                || left instanceof UnaryNode && ((UnaryNode) left).getOperator().morePrecedenceThan(operator.precedence)
                || left instanceof TernaryNode;
        boolean rightNeedsBrackets = right instanceof BinaryNode && (
                ((BinaryNode) right).operator == operator && operator.requiresBracketsWithSelf
                        || ((BinaryNode) right).operator.morePrecedenceThan(operator.precedence)
        )
                || right instanceof UnaryNode && ((UnaryNode) right).getOperator().morePrecedenceThan(operator.precedence)
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
