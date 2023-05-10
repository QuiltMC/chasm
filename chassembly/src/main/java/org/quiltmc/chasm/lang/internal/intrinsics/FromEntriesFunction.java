package org.quiltmc.chasm.lang.internal.intrinsics;

import org.quiltmc.chasm.lang.api.ast.Ast;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.eval.SourceSpan;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class FromEntriesFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof ListNode) {
            MapNode result = Ast.emptyMap();

            for (Node node : ((ListNode) arg).getEntries()) {
                if (node instanceof MapNode) {
                    MapNode mapNode = (MapNode) node;
                    Node key = mapNode.get("key");
                    Node value = mapNode.get("value");

                    if (!(key instanceof StringNode) || value == null) {
                        throw createException(arg);
                    }

                    result.put(((StringNode) key).getValue(), value);
                } else {
                    throw createException(arg);
                }
            }

            return result;
        }
        throw createException(arg);
    }

    private static EvaluationException createException(Node arg) {
        return new EvaluationException(
                "Built-in function \"from_entries\" can only be applied to lists of maps, "
                        + "each with {key: string, value: any}, but found " + arg,
                arg.getMetadata().get(SourceSpan.class)
        );
    }

    @Override
    public String getName() {
        return "from_entries";
    }
}
