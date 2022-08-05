package org.quiltmc.chasm.lang.api.eval;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.chasm.lang.api.ast.LambdaNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.ReferenceNode;
import org.quiltmc.chasm.lang.internal.eval.EvaluatorImpl;

@ApiStatus.NonExtendable
public interface Evaluator {
    static Evaluator create(Node node) {
        return new EvaluatorImpl(node);
    }

    Node resolveReference(ReferenceNode reference);

    ClosureNode createClosure(LambdaNode lambdaNode);

    Node callClosure(ClosureNode closure, Node arg);

    void pushTrace(Node beingEvaluated, String traceEntry);

    void popTrace();

    String renderTrace();
}
