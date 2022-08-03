package org.quiltmc.chasm.lang.internal.intrinsics;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class FromEntriesFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof ListNode) {
            List<Node> entries = ((ListNode) arg).getEntries();
            Map<String, Node> result = new LinkedHashMap<>();

            for (Node node : entries) {
                if (node instanceof MapNode) {
                    Map<String, Node> nodeEntries = ((MapNode) node).getEntries();
                    Node key = nodeEntries.get("key");
                    Node value = nodeEntries.get("value");

                    if (!(key instanceof StringNode) || value == null) {
                        throw createException(arg);
                    }

                    result.put(((StringNode) key).getValue(), value);
                } else {
                    throw createException(arg);
                }
            }

            return new MapNode(result);
        }
        throw createException(arg);
    }

    private static EvaluationException createException(Node arg) {
        return new EvaluationException(
                "Built-in function \"from_entries\" can only be applied to lists of maps, each with {key: string, value: any}, but found " + arg
        );
    }

    @Override
    String getName() {
        return "from_entries";
    }
}
