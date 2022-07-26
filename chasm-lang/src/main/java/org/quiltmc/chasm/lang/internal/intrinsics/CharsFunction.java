package org.quiltmc.chasm.lang.internal.intrinsics;

import java.util.ArrayList;
import java.util.List;

import org.quiltmc.chasm.lang.api.ast.IntegerNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.ast.ValueNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

class CharsFunction extends IntrinsicFunction {
    @Override
    String getName() {
        return "chars";
    }

    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (!(arg instanceof StringNode)) {
            throw new EvaluationException(
                    "Built-in function \"chars\" can only be applied to strings but found " + arg);
        }

        String value = ((StringNode) arg).getValue();

        List<Node> entries = new ArrayList<>();
        for (int i = 0; i < value.length(); i++) {
            entries.add(new IntegerNode((long) value.charAt(i)));
        }

        return new ListNode(entries);
    }
}
