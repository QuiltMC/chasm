package org.quiltmc.chasm.lang.internal.intrinsics;

import java.util.Map;
import java.util.stream.Collectors;

import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionNode;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

/**
 * The map intrinsic, which takes in a list and a function, and returns a new list of the result of applying the
 * function to each element in the list.
 */
public class MapFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (!(arg instanceof MapNode)) {
            throw createArgsException(arg);
        }

        Map<String, Node> args = ((MapNode) arg).getEntries();
        Node list = args.get("list");
        Node function = args.get("func");
        if (!(list instanceof ListNode) || !(function instanceof FunctionNode)) {
            throw createArgsException(arg);
        }

        return new ListNode(((ListNode) list).getEntries().stream()
                .map(entry -> ((FunctionNode) function).apply(evaluator, entry))
                .collect(Collectors.toList()));
    }

    @Override
    public String getName() {
        return "map";
    }

    private static EvaluationException createArgsException(Node arg) {
        return new EvaluationException(
                "Built-in function \"map\" can only be applied to args {list, func} but found " + arg,
                arg.getMetadata().get(SourceSpan.class)
        );
    }
}
