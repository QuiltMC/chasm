package org.quiltmc.chasm.lang.internal.eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.LiteralNode;
import org.quiltmc.chasm.lang.api.ast.Node;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.FunctionNode;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class Intrinsics {
    static final Map<String, Node> INTRINSICS;

    static {
        INTRINSICS = new HashMap<>();
        INTRINSICS.put("chars", new CharsFunction());
        INTRINSICS.put("join", new JoinFunction());
    }

    static class CharsFunction extends FunctionNode {
        @Override
        public Node apply(Evaluator evaluator, Node node) {
            if (!(node instanceof LiteralNode)
                    || !(((LiteralNode) node).getValue() instanceof String)) {
                throw new EvaluationException(
                        "Built-in function \"chars\" can only be applied to Strings but found " + node);
            }

            String value = (String) ((LiteralNode) node).getValue();

            List<Node> entries = new ArrayList<>();
            for (int i = 0; i < value.length(); i++) {
                entries.add(new LiteralNode((long) value.charAt(i)));
            }

            return new ListNode(entries);
        }

        @Override
        public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {

        }
    }

    static class JoinFunction extends FunctionNode {
        @Override
        public Node apply(Evaluator evaluator, Node node) {
            if (!(node instanceof ListNode)) {
                throw new EvaluationException(
                        "Built-in function \"join\" can only be applied to list of integers but found " + node);
            }

            List<Node> entries = ((ListNode) node).getEntries();

            if (!entries.stream().allMatch(
                    e -> e instanceof LiteralNode && ((LiteralNode) e).getValue() instanceof Long)) {
                throw new EvaluationException(
                        "Built-in function \"join\" can only be applied to list of integers but found " + node);
            }

            String joined = entries.stream()
                    .map(e -> Character.toString((char) ((Long) ((LiteralNode) e).getValue()).shortValue()))
                    .collect(Collectors.joining());

            return new LiteralNode(joined);
        }

        @Override
        public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {

        }
    }
}
