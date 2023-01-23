package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionNode;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class ReduceFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof MapNode) {
            MapNode args = (MapNode) arg;
            Node list = args.get("list");
            Node function = args.get("func");

            if (list instanceof ListNode && function instanceof FunctionNode) {
                return ((ListNode) list).getEntries().stream().reduce((first, second) -> {
                    MapNode funcArgs = Ast.map().put("first", first).put("second", second).build();
                    return ((FunctionNode) function).apply(evaluator, funcArgs);
                }).orElseThrow(() -> new EvaluationException(
                        "Can't reduce empty list: " + list
                ));
            }
        }

        throw new EvaluationException(
                "Built-in function \"reduce\" can only be applied to args {list, func} but found " + arg
        );
    }

    @Override
    public String getName() {
        return "reduce";
    }
}
