package org.quiltmc.chasm.lang.api.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.OperatorPriority;
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
                if (left instanceof ListNode && right instanceof ListNode) {
                    List<Node> leftEntries = ((ListNode) left).getEntries();
                    List<Node> rightEntries = ((ListNode) right).getEntries();

                    ArrayList<Node> newEntries = new ArrayList<>(leftEntries);
                    newEntries.addAll(rightEntries);

                    return new ListNode(newEntries);
                }

                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return new IntegerNode(((IntegerNode) left).getValue() + ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return new FloatNode(((FloatNode) left).getValue() + ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return new FloatNode(((IntegerNode) left).getValue() + ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return new FloatNode(((FloatNode) left).getValue() + ((IntegerNode) right).getValue());
                }

                if (left instanceof StringNode && right instanceof ValueNode) {
                    return new StringNode(((StringNode) left).getValue() + ((ValueNode<?>) right).getValue());
                }

                if (left instanceof ValueNode && right instanceof StringNode) {
                    return new StringNode(((ValueNode<?>) left).getValue() + ((StringNode) right).getValue());
                }
            }
            break;
            case MINUS: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return new IntegerNode(((IntegerNode) left).getValue() - ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return new FloatNode(((FloatNode) left).getValue() - ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return new FloatNode(((IntegerNode) left).getValue() - ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return new FloatNode(((FloatNode) left).getValue() - ((IntegerNode) right).getValue());
                }
            }
            break;
            case MULTIPLY: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return new IntegerNode(((IntegerNode) left).getValue() * ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return new FloatNode(((FloatNode) left).getValue() * ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return new FloatNode(((IntegerNode) left).getValue() * ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return new FloatNode(((FloatNode) left).getValue() * ((IntegerNode) right).getValue());
                }
            }
            break;
            case DIVIDE: {

                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return new IntegerNode(((IntegerNode) left).getValue() / ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return new FloatNode(((FloatNode) left).getValue() / ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return new FloatNode(((IntegerNode) left).getValue() / ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return new FloatNode(((FloatNode) left).getValue() / ((IntegerNode) right).getValue());
                }
            }
            break;
            case MODULO: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return new IntegerNode(((IntegerNode) left).getValue() % ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return new FloatNode(((FloatNode) left).getValue() % ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return new FloatNode(((IntegerNode) left).getValue() % ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return new FloatNode(((FloatNode) left).getValue() % ((IntegerNode) right).getValue());
                }
            }
            break;
            case SHIFT_LEFT: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return new IntegerNode(((IntegerNode) left).getValue() << ((IntegerNode) right).getValue());
                }
            }
            break;
            case SHIFT_RIGHT: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return new IntegerNode(((IntegerNode) left).getValue() >> ((IntegerNode) right).getValue());
                }
            }
            break;
            case SHIFT_RIGHT_UNSIGNED: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return new IntegerNode(((IntegerNode) left).getValue() >>> ((IntegerNode) right).getValue());
                }
            }
            break;
            case LESS_THAN: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return BooleanNode.from(((IntegerNode) left).getValue() < ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return BooleanNode.from(((FloatNode) left).getValue() < ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return BooleanNode.from(((IntegerNode) left).getValue() < ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return BooleanNode.from(((FloatNode) left).getValue() < ((IntegerNode) right).getValue());
                }
            }
            break;
            case LESS_THAN_OR_EQUAL: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return BooleanNode.from(((IntegerNode) left).getValue() <= ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return BooleanNode.from(((FloatNode) left).getValue() <= ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return BooleanNode.from(((IntegerNode) left).getValue() <= ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return BooleanNode.from(((FloatNode) left).getValue() <= ((IntegerNode) right).getValue());
                }
            }
            break;
            case GREATER_THAN: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return BooleanNode.from(((IntegerNode) left).getValue() > ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return BooleanNode.from(((FloatNode) left).getValue() > ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return BooleanNode.from(((IntegerNode) left).getValue() > ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return BooleanNode.from(((FloatNode) left).getValue() > ((IntegerNode) right).getValue());
                }
            }
            break;
            case GREATER_THAN_OR_EQUAL: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return BooleanNode.from(((IntegerNode) left).getValue() >= ((IntegerNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof FloatNode) {
                    return BooleanNode.from(((FloatNode) left).getValue() >= ((FloatNode) right).getValue());
                }

                if (left instanceof IntegerNode && right instanceof FloatNode) {
                    return BooleanNode.from(((IntegerNode) left).getValue() >= ((FloatNode) right).getValue());
                }

                if (left instanceof FloatNode && right instanceof IntegerNode) {
                    return BooleanNode.from(((FloatNode) left).getValue() >= ((IntegerNode) right).getValue());
                }
            }
            break;
            case EQUAL: {
                if (left instanceof ValueNode && right instanceof ValueNode) {
                    return BooleanNode.from(
                            Objects.equals(((ValueNode<?>) left).getValue(), ((ValueNode<?>) right).getValue())
                    );
                }
            }
            break;
            case NOT_EQUAL: {
                if (left instanceof ValueNode && right instanceof ValueNode) {
                    return BooleanNode.from(
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
                    return new IntegerNode(((IntegerNode) left).getValue() & ((IntegerNode) right).getValue());
                }
            }
            break;
            case BITWISE_XOR: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return new IntegerNode(((IntegerNode) left).getValue() ^ ((IntegerNode) right).getValue());
                }
            }
            break;
            case BITWISE_OR: {
                if (left instanceof IntegerNode && right instanceof IntegerNode) {
                    return new IntegerNode(((IntegerNode) left).getValue() | ((IntegerNode) right).getValue());
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
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier, OperatorPriority minPriority) {
        boolean needsBrackets = !this.operator.precedence.allowedFor(minPriority);
        if (needsBrackets) {
            builder.append('(');
        }

        left.render(renderer, builder, currentIndentationMultiplier, this.operator.precedence);

        builder.append(' ').append(operator.image).append(' ');

        right.render(renderer, builder, currentIndentationMultiplier, this.operator.precedence.inc());
        if (needsBrackets) {
            builder.append(')');
        }
    }

    public enum Operator {
        PLUS("+", OperatorPriority.ADDITION),
        MINUS("-", OperatorPriority.ADDITION),
        MULTIPLY("*", OperatorPriority.MULTIPLICATIVE),
        DIVIDE("/", OperatorPriority.MULTIPLICATIVE),
        MODULO("%", OperatorPriority.MULTIPLICATIVE),
        SHIFT_LEFT("<<", OperatorPriority.SHIFT),
        SHIFT_RIGHT(">>", OperatorPriority.SHIFT),
        SHIFT_RIGHT_UNSIGNED(">>>", OperatorPriority.SHIFT),
        LESS_THAN("<", OperatorPriority.RELATIONAL),
        LESS_THAN_OR_EQUAL("<=", OperatorPriority.RELATIONAL),
        GREATER_THAN(">", OperatorPriority.RELATIONAL),
        GREATER_THAN_OR_EQUAL(">=", OperatorPriority.RELATIONAL),
        EQUAL("=", OperatorPriority.EQUALITY),
        NOT_EQUAL("!=", OperatorPriority.EQUALITY),
        BITWISE_AND("&", OperatorPriority.BITWISE_AND),
        BITWISE_XOR("^", OperatorPriority.BITWISE_XOR),
        BITWISE_OR("|", OperatorPriority.BITWISE_OR),
        BOOLEAN_AND("&&", OperatorPriority.BOOLEAN_AND),
        BOOLEAN_OR("||", OperatorPriority.BOOLEAN_OR);

        private final String image;
        private final OperatorPriority precedence;

        Operator(String image, OperatorPriority precedence) {
            this.image = image;
            this.precedence = precedence;
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
