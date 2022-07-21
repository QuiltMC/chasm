package org.quiltmc.chasm.lang.api.ast;

import org.quiltmc.chasm.lang.internal.render.RendererConfig;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionNode;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class CallNode extends Node {
    private Node function;
    private Node arg;

    public CallNode(Node function, Node arg) {
        this.function = function;
        this.arg = arg;
    }

    public Node getFunction() {
        return function;
    }

    public void setFunction(Node function) {
        this.function = function;
    }

    public Node getArg() {
        return arg;
    }

    public void setArg(Node arg) {
        this.arg = arg;
    }

    @Override
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        function.render(config, builder, currentIndentationMultiplier);
        builder.append('(');
        arg.render(config, builder, currentIndentationMultiplier);
        builder.append(')');
    }

    public CallNode copy() {
        return new CallNode(function.copy(), arg.copy());
    }

    @Override
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
        function.resolve(resolver);
        arg.resolve(resolver);
    }

    @Override
    public Node evaluate(Evaluator evaluator) {
        Node function = this.function.evaluate(evaluator);

        if (function instanceof FunctionNode) {
            return ((FunctionNode) function).apply(evaluator, arg.evaluate(evaluator));
        }

        if (function instanceof ClosureNode) {
            return evaluator.callClosure((ClosureNode) function, arg.evaluate(evaluator));
        }

        throw new EvaluationException("Can only call functions and closures, but found " + function);
    }
}
