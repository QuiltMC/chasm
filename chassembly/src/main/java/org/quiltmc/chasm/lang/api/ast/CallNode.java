package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionNode;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * A call expression, representing function application.
 */
public class CallNode extends Node {
    private Node function;
    private Node arg;

    /**
     * Creates a call expression.
     *
     * @see Ast#call(Node, Node)
     */
    public CallNode(Node function, Node arg) {
        this.function = function;
        this.arg = arg;
    }

    /**
     * Gets the function that is being applied.
     */
    public Node getFunction() {
        return function;
    }

    /**
     * Sets the function that is being applied.
     */
    public void setFunction(Node function) {
        this.function = function;
    }

    /**
     * Gets the argument to the application.
     */
    public Node getArg() {
        return arg;
    }

    /**
     * Sets the argument to the application.
     */
    public void setArg(Node arg) {
        this.arg = arg;
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        function.render(renderer, builder, currentIndentationMultiplier);
        builder.append('(');
        arg.render(renderer, builder, currentIndentationMultiplier);
        builder.append(')');
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

        throw new EvaluationException(
                "Can only call functions but found " + function.typeName(),
                function.getMetadata().get(SourceSpan.class)
        );
    }

    @Override
    public String typeName() {
        return "call expression";
    }
}
