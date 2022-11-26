package org.quiltmc.chasm.lang.api.eval;

import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * Represents a lambda with all captures resolved. Do not create directly; if you really need to create a closure,
 * use {@linkplain Evaluator#createClosure(LambdaNode)}.
 */
public class ClosureNode extends FunctionNode {
    private final LambdaNode lambda;
    private final Map<String, Node> captures;

    @ApiStatus.Internal
    public ClosureNode(LambdaNode lambda, Map<String, Node> captures) {
        this.lambda = lambda;
        this.captures = captures;
    }

    /**
     * Gets the lambda for this closure.
     */
    public LambdaNode getLambda() {
        return lambda;
    }

    /**
     * Gets the captures for this closure.
     */
    public Map<String, Node> getCaptures() {
        return captures;
    }

    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        return evaluator.callClosure(this, arg);
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        // Represent a closure as a capture-free lambda
        // Note: This currently fails because of infinite recursion

        /*
        String bodyName = "__lambda_body";
        MapNode mapNode = new MapNode(new HashMap<>(captures));
        mapNode.getEntries().put(bodyName, lambda.getInner());
        IndexNode indexNode = new IndexNode(mapNode, new LiteralNode(bodyName));
        LambdaNode lambdaNode = new LambdaNode(lambda.getIdentifier(), indexNode);
        lambdaNode.render(config, builder, currentIndentationMultiplier);
        */

        builder.append("<Closure can't be rendered>");
    }
}
