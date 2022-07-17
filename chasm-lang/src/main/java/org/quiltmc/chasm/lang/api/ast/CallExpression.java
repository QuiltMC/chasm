package org.quiltmc.chasm.lang.api.ast;

import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class CallExpression extends Expression {
    private Expression function;
    private Expression arg;

    public CallExpression(Expression function, Expression arg) {
        this.function = function;
        this.arg = arg;
    }

    public Expression getFunction() {
        return function;
    }

    public void setFunction(Expression function) {
        this.function = function;
    }

    public Expression getArg() {
        return arg;
    }

    public void setArg(Expression arg) {
        this.arg = arg;
    }

    @Override
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        function.render(config, builder, currentIndentationMultiplier);
        builder.append('(');
        arg.render(config, builder, currentIndentationMultiplier);
        builder.append(')');
    }

    @Override
    public CallExpression copy() {
        return new CallExpression(function.copy(), arg.copy());
    }
}
