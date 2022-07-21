package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public class TernaryNode extends Node {
    private Node condition;
    private Node trueExp;
    private Node falseExp;

    public TernaryNode(Node condition, Node trueExp, Node falseExp) {
        this.condition = condition;
        this.trueExp = trueExp;
        this.falseExp = falseExp;
    }

    public Node getCondition() {
        return condition;
    }

    public void setCondition(Node condition) {
        this.condition = condition;
    }

    public Node getTrue() {
        return trueExp;
    }

    public void setTrue(Node trueExp) {
        this.trueExp = trueExp;
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

    public Node getFalse() {
        return falseExp;
    }

    public void setFalse(Node falseExp) {
        this.falseExp = falseExp;
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

        if (!(condition instanceof LiteralNode)
                || !(((LiteralNode) condition).getValue() instanceof Boolean)) {
            throw new EvaluationException("Condition in ternary must evaluate to a boolean but found " + condition);
        }

        if ((Boolean) ((LiteralNode) condition).getValue()) {
            return trueExp.evaluate(evaluator);
        } else {
            return falseExp.evaluate(evaluator);
        }
    }
}
