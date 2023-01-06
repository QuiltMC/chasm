package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * A ternary expression, e.g. {@code foo ? bar : baz}.
 */
public class TernaryNode extends Node {
    private Node condition;
    private Node trueExp;
    private Node falseExp;

    /**
     * Creates a ternary expression.
     *
     * @see Ast#ternary(Node, Node, Node)
     */
    public TernaryNode(Node condition, Node trueExp, Node falseExp) {
        this.condition = condition;
        this.trueExp = trueExp;
        this.falseExp = falseExp;
    }

    /**
     * Gets the condition.
     */
    public Node getCondition() {
        return condition;
    }

    /**
     * Sets the condition.
     */
    public void setCondition(Node condition) {
        this.condition = condition;
    }

    /**
     * Gets the expression returned if the condition is true.
     */
    public Node getTrue() {
        return trueExp;
    }

    /**
     * Sets the expression returned if the condition is true.
     */
    public void setTrue(Node trueExp) {
        this.trueExp = trueExp;
    }

    /**
     * Gets the expression returned if the condition is false.
     */
    public Node getFalse() {
        return falseExp;
    }

    /**
     * Sets the expression returned if the condition is false.
     */
    public void setFalse(Node falseExp) {
        this.falseExp = falseExp;
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        boolean wrapWithBraces = condition instanceof TernaryNode;
        if (wrapWithBraces) {
            builder.append('(');
        }
        condition.render(renderer, builder, currentIndentationMultiplier);
        if (wrapWithBraces) {
            builder.append(')');
        }
        builder.append(" ? ");
        trueExp.render(renderer, builder, currentIndentationMultiplier);
        builder.append(" : ");
        falseExp.render(renderer, builder, currentIndentationMultiplier);
    }

    @Override
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
        condition.resolve(resolver);
        trueExp.resolve(resolver);
        falseExp.resolve(resolver);
    }

    @Override
    @ApiStatus.OverrideOnly
    public Node evaluate(Evaluator evaluator) {
        Node condition = this.condition.evaluate(evaluator);

        if (!(condition instanceof BooleanNode)) {
            throw new EvaluationException("Condition in ternary must evaluate to a boolean but found " + condition);
        }

        if (((BooleanNode) condition).getValue()) {
            return trueExp.evaluate(evaluator);
        } else {
            return falseExp.evaluate(evaluator);
        }
    }
}
