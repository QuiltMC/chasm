package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.OperatorPriority;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public class UnaryNode extends Node {
    private Node inner;
    private Operator operator;

    public UnaryNode(Node inner, Operator operator) {
        this.inner = inner;
        this.operator = operator;
    }

    public Node getInner() {
        return inner;
    }

    public void setInner(Node inner) {
        this.inner = inner;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier,
                       OperatorPriority minPriority) {
        boolean needsBrackets = !OperatorPriority.UNARY.allowedFor(minPriority);
        if (needsBrackets) {
            builder.append('(');
        }

        builder.append(operator.image);
        inner.render(renderer, builder, currentIndentationMultiplier, OperatorPriority.UNARY);

        if (needsBrackets) {
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
