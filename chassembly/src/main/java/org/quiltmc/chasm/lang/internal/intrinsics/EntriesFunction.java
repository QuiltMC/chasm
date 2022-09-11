package org.quiltmc.chasm.lang.internal.intrinsics;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.ast.StringNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.IntrinsicFunction;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class EntriesFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof MapNode) {
            List<Node> entryList = new ArrayList<>();

            ((MapNode) arg).getEntries().forEach((key, value) -> {
                Map<String, Node> entry = new LinkedHashMap<>();
                entry.put("key", new StringNode(key));
                entry.put("value", value);

                entryList.add(new MapNode(entry));
            });

            return new ListNode(entryList);
        }
        throw new EvaluationException("Built-in function \"entries\" can only be applied to maps but found " + arg);
    }

    @Override
    public String getName() {
        return "entries";
    }
}
