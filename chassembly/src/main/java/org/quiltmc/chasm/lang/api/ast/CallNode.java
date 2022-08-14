package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionNode;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.OperatorPriority;
import org.quiltmc.chasm.lang.internal.render.Renderer;

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
    public void render(Renderer renderer, StringBuilder builder, int indentation, OperatorPriority minPriority) {
        boolean needsBrackets = !OperatorPriority.ARGUMENT_PRIMARY.allowedFor(minPriority);
        if (needsBrackets) {
            builder.append('(');
        }
        function.render(renderer, builder, indentation, OperatorPriority.ARGUMENT_PRIMARY);
        builder.append('(');
        arg.render(renderer, builder, indentation, OperatorPriority.ANY);
        builder.append(')');
        if (needsBrackets) {
            builder.append(')');
        }
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

        throw new EvaluationException("Can only call functions but found " + function);
    }
}
