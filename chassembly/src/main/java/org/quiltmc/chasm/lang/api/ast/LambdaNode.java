package org.quiltmc.chasm.lang.api.ast;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.OperatorPriority;
import org.quiltmc.chasm.lang.internal.render.Renderer;

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
    public void render(Renderer renderer, StringBuilder builder, int indentation, OperatorPriority minPriority) {
        boolean needsBrackets = !OperatorPriority.ANY.allowedFor(minPriority);
        if (needsBrackets) {
            builder.append('(');
        }
        builder.append(identifier);
        builder.append(" -> ");
        inner.render(renderer, builder, indentation, OperatorPriority.ANY);
        if (needsBrackets) {
            builder.append(')');
        }
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
