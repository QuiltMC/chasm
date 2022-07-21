package org.quiltmc.chasm.lang.api.ast;

import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class TernaryExpression extends Expression {
    private Expression condition;
    private Expression trueExp;
    private Expression falseExp;

    public TernaryExpression(Expression condition, Expression trueExp, Expression falseExp) {
        this.condition = condition;
        this.trueExp = trueExp;
        this.falseExp = falseExp;
    }

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public Expression getTrue() {
        return trueExp;
    }

    public void setTrue(Expression trueExp) {
        this.trueExp = trueExp;
    }

    @Override
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        boolean wrapWithBraces = condition instanceof TernaryExpression;
        if (wrapWithBraces) {
            builder.append('(');
        }
        condition.render(config, builder, currentIndentationMultiplier);
        if (wrapWithBraces) {
            builder.append(')');
        }
        builder.append(" ? ");
        trueExp.render(config, builder, currentIndentationMultiplier);
        builder.append(" : ");
        falseExp.render(config, builder, currentIndentationMultiplier);
    }

    public Expression getFalse() {
        return falseExp;
    }

    public void setFalse(Expression falseExp) {
        this.falseExp = falseExp;
    }

    @Override
    public TernaryExpression copy() {
        return new TernaryExpression(condition.copy(), trueExp.copy(), falseExp.copy());
    }
}
