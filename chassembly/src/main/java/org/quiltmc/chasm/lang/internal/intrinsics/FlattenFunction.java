package org.quiltmc.chasm.lang.internal.intrinsics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

/**
 * The flatten intrinsic, which takes in a list of lists and returns a new list with the contents of the sublists.
 */
public class FlattenFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof ListNode) {
            ListNode listArg = ((ListNode) arg);
            Node firstElement = listArg.getEntries().get(0);

            if (firstElement instanceof ListNode) {
                return new ListNode(listArg.getEntries()
                        .stream()
                        .map(entry -> {
                            if (entry instanceof ListNode) {
                                return (ListNode) entry;
                            } else {
                                throw createArgsException(arg);
                            }
                        })
                        .flatMap(entry -> entry.getEntries().stream())
                        .collect(Collectors.toList()));
            }

            if (firstElement instanceof MapNode) {
                Map<String, Node> result = new LinkedHashMap<>();
                listArg.getEntries().stream().map(entry -> {
                    if (entry instanceof MapNode) {
                        return (MapNode) entry;
                    } else {
                        throw createArgsException(arg);
                    }
                }).forEach(node -> result.putAll(node.getEntries()));

                return new MapNode(result);
            }
        }

        throw createArgsException(arg);
    }

    @Override
    String getName() {
        return "flatten";
    }

    private static EvaluationException createArgsException(Node arg) {
        return new EvaluationException(
                "Built-in function \"flatten\" can only be applied to lists of lists or lists of maps but found " + arg
        );
    }
}
