package org.quiltmc.chasm.lang.internal.intrinsics;

import java.util.Map;

import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionNode;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class MapFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (!(arg instanceof MapNode)) {
            throw createArgsException(arg);
        }

        Map<String, Node> args = ((MapNode) arg).getEntries();
        Node list = args.get("list");
        Node function = args.get("function");
        if (!(list instanceof ListNode) || !(function instanceof FunctionNode)) {
            throw createArgsException(arg);
        }

        return ((ListNode) list).getEntries().stream()
                .map(entry -> ((FunctionNode) function).apply(evaluator, entry))
                .collect(ListNode.collector());
    }

    @Override
    String getName() {
        return "map";
    }

    private static EvaluationException createArgsException(Node arg) {
        return new EvaluationException(
                "Built-in function \"map\" can only be applied to args {list, function} but found " + arg
        );
    }
}
