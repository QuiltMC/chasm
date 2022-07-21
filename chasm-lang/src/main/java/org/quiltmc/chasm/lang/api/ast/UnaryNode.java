package org.quiltmc.chasm.lang.api.ast;

import org.quiltmc.chasm.lang.internal.render.RendererConfig;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

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
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append(operator.image);
        boolean wrapWithBraces = inner instanceof BinaryNode && ((BinaryNode) inner).getOperator().morePrecedenceThan(operator.precedence) // we don't have to do the funky requiresBracketsWithSelf here luckily
                              || inner instanceof UnaryNode && ((UnaryNode) inner).operator.morePrecedenceThan(operator.precedence)
                              || inner instanceof TernaryNode;

        if (wrapWithBraces) {
            builder.append('(');
        }
        inner.render(config, builder, currentIndentationMultiplier);
        if (wrapWithBraces) {
            builder.append(')');
        }
    }

    public UnaryNode copy() {
        return new UnaryNode(inner.copy(), operator);
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

        if (!(inner instanceof LiteralNode)) {
            throw new EvaluationException("Unary expression can only be applied to literal values but found " + inner);
        }

        Object value = ((LiteralNode) inner).getValue();

        switch (operator) {
            case PLUS: {
                if (value instanceof Long || value instanceof Double) {
                    return inner;
                }
                throw new EvaluationException(
                        "Unary plus operator can only be applied to integers and floats but found " + value.getClass()
                );
            }
            case MINUS: {
                if (value instanceof Long) {
                    return new LiteralNode(-(Long) value);
                }
                if (value instanceof Double) {
                    return new LiteralNode(-(Double) value);
                }
                throw new EvaluationException(
                        "Unary minus operator can only be applied to integers and floats but found " + value.getClass()
                );
            }
            case NOT: {
                if (value instanceof Boolean) {
                    return new LiteralNode(!(Boolean) value);
                }
                throw new EvaluationException(
                        "Unary not operator can only be applied to booleans but found " + value.getClass()
                );
            }
            case INVERT: {
                if (value instanceof Long) {
                    return new LiteralNode(~(Long) value);
                }
                throw new EvaluationException(
                        "Unary invert operator can only be applied to integers but found " + value.getClass()
                );
            }
            default: {
                throw new EvaluationException(
                        "Unknown unary operator " + operator
                );
            }
        }
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

        public boolean morePrecedenceThan(int precedence) {
            return this.precedence > precedence;
        }
    }
}
