package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * A lambda expression.
 */
public class LambdaNode extends Node {
    private String identifier;
    private Node inner;

    /**
     * Creates a lambda expression.
     *
     * @see Ast#lambda(String, Node)
     */
    public LambdaNode(String identifier, Node inner) {
        this.identifier = identifier;
        this.inner = inner;
    }

    /**
     * Gets the argument name of this lambda expression.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the argument name of this lambda.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the body of this lambda.
     */
    public Node getInner() {
        return inner;
    }

    /**
     * Sets the body of this lambda.
     */
    public void setInner(Node inner) {
        this.inner = inner;
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append(identifier);
        builder.append(" -> ");
        inner.render(renderer, builder, currentIndentationMultiplier);
    }

    @Override
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
        resolver.enterLambda(this);
        inner.resolve(resolver);
        resolver.exitLambda();
    }

    @Override
    @ApiStatus.OverrideOnly
    public Node evaluate(Evaluator evaluator) {
        return evaluator.createClosure(this);
    }

    @Override
    public String typeName() {
        return "lambda expression";
    }
}
