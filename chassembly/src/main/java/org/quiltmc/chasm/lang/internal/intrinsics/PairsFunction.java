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
import org.quiltmc.chasm.lang.api.exception.EvaluationException;

public class PairsFunction extends IntrinsicFunction {
    @Override
    public Node apply(Evaluator evaluator, Node arg) {
        if (arg instanceof MapNode) {
            List<Node> pairList = new ArrayList<>();

            ((MapNode) arg).getEntries().forEach((key, value) -> {
                Map<String, Node> pair = new LinkedHashMap<>();
                pair.put("key", new StringNode(key));
                pair.put("value", value);

                pairList.add(new MapNode(pair));
            });

            return new ListNode(pairList);
        }
        throw new EvaluationException("Built-in function \"pairs\" can only be applied to maps but found " + arg);
    }

    @Override
    String getName() {
        return "pairs";
    }
}
