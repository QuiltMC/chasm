package org.quiltmc.chasm.lang.api.ast;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class ClosureNode extends Node {
    private final LambdaNode lambda;
    private final Map<String, Node> captures;

    public ClosureNode(LambdaNode lambda, Map<String, Node> captures) {
        this.lambda = lambda;
        this.captures = captures;
    }

    public LambdaNode getLambda() {
        return lambda;
    }

    public Map<String, Node> getCaptures() {
        return captures;
    }

    @Override
    public Node copy() {
        throw new UnsupportedOperationException();
    }

    @Override
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
        throw new EvaluationException("Closures can't be resolved");
    }

    @Override
    @ApiStatus.OverrideOnly
    public Node evaluate(Evaluator evaluator) {
        return this;
    }

    @Override
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        throw new UnsupportedOperationException();
    }
}
