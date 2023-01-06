package org.quiltmc.chasm.lang.api.eval;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.ReferenceNode;
import org.quiltmc.chasm.lang.internal.eval.EvaluatorImpl;
import org.quiltmc.chasm.lang.internal.intrinsics.BuiltInIntrinsics;

/**
 * Helper for evaluating (reducing) nodes.
 */
@ApiStatus.NonExtendable
public interface Evaluator {
    /**
     * Creates an evaluator with the default settings for the given root node.
     */
    static Evaluator create(Node node) {
        return new EvaluatorImpl(node, BuiltInIntrinsics.ALL);
    }

    /**
     * Creates an evaluator builder for the given root node, with which you can customize the evaluator's settings.
     */
    static Builder builder(Node node) {
        return new EvaluatorImpl.Builder(node);
    }

    /**
     * Resolves a reference node to the node it is referring to.
     */
    Node resolveReference(ReferenceNode reference);

    /**
     * Creates a closure from a lambda node.
     *
     * @see ClosureNode
     */
    ClosureNode createClosure(LambdaNode lambdaNode);

    /**
     * Applies a closure using the given argument.
     */
    Node callClosure(ClosureNode closure, Node arg);

    /**
     * Gets the {@linkplain Resolver} of this evaluator.
     */
    Resolver getResolver();

    /**
     * An evaluator builder to customize the settings of an evaluator.
     */
    @ApiStatus.NonExtendable
    interface Builder {
        /**
         * Adds a new intrinsic function.
         */
        Builder addIntrinsic(IntrinsicFunction intrinsic);

        /**
         * Builds the evaluator.
         */
        Evaluator build();
    }
}
