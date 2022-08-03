package org.quiltmc.chasm.lang.internal.intrinsics;

import java.util.LinkedHashMap;
import java.util.Map;

import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.NullNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionNode;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class ReduceFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof MapNode) {
            Map<String, Node> args = ((MapNode) arg).getEntries();
            Node list = args.get("list");
            Node function = args.get("func");

            if (list instanceof ListNode && function instanceof FunctionNode) {
                Map<String, Node> funcArgs = new LinkedHashMap<>();
                MapNode funcArgsNode = new MapNode(funcArgs);

                return ((ListNode) list).getEntries().stream().reduce((first, second) -> {
                    funcArgs.put("first", first);
                    funcArgs.put("second", second);

                    return ((FunctionNode) function).apply(evaluator, funcArgsNode);
                }).orElse(NullNode.INSTANCE);
            }
        }

        throw new EvaluationException(
                "Built-in function \"reduce\" can only be applied to args {list, func} but found " + arg
        );
    }

    @Override
    String getName() {
        return "reduce";
    }
}
