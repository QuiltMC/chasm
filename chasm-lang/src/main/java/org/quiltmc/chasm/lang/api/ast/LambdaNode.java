package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class LambdaNode extends Node {
    private String identifier;
    private Node inner;

    public LambdaNode(String identifier, Node inner) {
        this.identifier = identifier;
        this.inner = inner;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public Node getInner() {
        return inner;
    }

    public void setInner(Node inner) {
        this.inner = inner;
    }

    @Override
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append(identifier);
        builder.append(" -> ");
        inner.render(config, builder, currentIndentationMultiplier);
    }

    public LambdaNode copy() {
        return new LambdaNode(identifier, inner.copy());
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
}
