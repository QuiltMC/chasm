package org.quiltmc.chasm.lang.api.eval;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.ReferenceNode;
import org.quiltmc.chasm.lang.internal.eval.EvaluatorImpl;
import org.quiltmc.chasm.lang.internal.intrinsics.BuiltInIntrinsics;

@ApiStatus.NonExtendable
public interface Evaluator {
    static Evaluator create(Node node) {
        return new EvaluatorImpl(node, BuiltInIntrinsics.ALL);
    }

    static Builder builder(Node node) {
        return new EvaluatorImpl.Builder(node);
    }

    Node resolveReference(ReferenceNode reference);

    ClosureNode createClosure(LambdaNode lambdaNode);

    Node callClosure(ClosureNode closure, Node arg);

    Resolver getResolver();

    @ApiStatus.NonExtendable
    interface Builder {
        Builder addIntrinsic(IntrinsicFunction intrinsic);

        Evaluator build();
    }
}
