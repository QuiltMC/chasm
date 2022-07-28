package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionNode;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

import java.util.Map;

public class FlattenFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (!(arg instanceof ListNode)) {
            throw createArgsException(arg);
        }

        return ((ListNode) arg).getEntries()
                .stream()
                .map(entry -> {
                    if (entry instanceof ListNode) {
                        return (ListNode) entry;
                    } else {
                        throw createArgsException(arg);
                    }
                })
                .flatMap(entry -> entry.getEntries().stream())
                .collect(ListNode.collector());
    }

    @Override
    String getName() {
        return "flatten";
    }

    private static EvaluationException createArgsException(Node arg) {
        return new EvaluationException("Built-in function \"flatten\" can only be applied to lists of lists but found " + arg);
    }
}
