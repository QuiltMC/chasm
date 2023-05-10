package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionNode;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class ReduceFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof MapNode) {
            MapNode args = (MapNode) arg;
            Node list = args.get("list");
            Node function = args.get("func");

            if (list instanceof ListNode && function instanceof FunctionNode) {
                ListNode listNode = (ListNode) list;
                if (listNode.size() == 0) {
                    throw new EvaluationException(
                            "Can't reduce empty list: " + list,
                            arg.getMetadata().get(SourceSpan.class)
                    );
                }
                FunctionNode funcNode = (FunctionNode) function;

                Node result = listNode.get(0);
                for (int i = 1; i < listNode.size(); i++) {
                    MapNode funcArgs = Ast.map().put("first", result).put("second", listNode.get(i)).build();
                    result = funcNode.apply(evaluator, funcArgs);
                }

                return result;
            }
        }

        throw new EvaluationException(
                "Built-in function \"reduce\" can only be applied to args {list, func} but found " + arg,
                arg.getMetadata().get(SourceSpan.class)
        );
    }

    @Override
    public String getName() {
        return "reduce";
    }
}
