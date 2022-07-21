package org.quiltmc.chasm.lang.internal.intrinsics;

import java.util.List;
import java.util.stream.Collectors;

import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.LiteralNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

class JoinFunction extends IntrinsicFunction {
    @Override
    String getName() {
        return "join";
    }

    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (!(arg instanceof ListNode)) {
            throw new EvaluationException(
                    "Built-in function \"join\" can only be applied to list of integers but found " + arg);
        }

        List<Node> entries = ((ListNode) arg).getEntries();

        if (!entries.stream().allMatch(
                e -> e instanceof LiteralNode && ((LiteralNode) e).getValue() instanceof Long)) {
            throw new EvaluationException(
                    "Built-in function \"join\" can only be applied to list of integers but found " + arg);
        }

        String joined = entries.stream()
                .map(e -> Character.toString((char) ((Long) ((LiteralNode) e).getValue()).shortValue()))
                .collect(Collectors.joining());

        return new LiteralNode(joined);
    }
}
