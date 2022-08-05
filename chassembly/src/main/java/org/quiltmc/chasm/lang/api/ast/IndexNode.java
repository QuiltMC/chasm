package org.quiltmc.chasm.lang.api.ast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.ClosureNode;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.api.exception.EvaluationException;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public class IndexNode extends Node {
    private Node left;
    private Node index;

    public IndexNode(Node left, Node index) {
        this.left = left;
        this.index = index;
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node node) {
        this.left = node;
    }

    public Node getIndex() {
        return index;
    }

    public void setIndex(Node index) {
        this.index = index;
    }

    @Override
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
        left.resolve(resolver);
        index.resolve(resolver);
    }

    @Override
    @ApiStatus.OverrideOnly
    public Node evaluate(Evaluator evaluator) {
        Node leftNode = this.left.evaluate(evaluator);

        evaluator.pushTrace(this.index, "index");
        Node indexNode = this.index.evaluate(evaluator);
        evaluator.popTrace();

        // Index list
        if (leftNode instanceof ListNode && indexNode instanceof IntegerNode) {
            long index = ((IntegerNode) indexNode).getValue();
            List<Node> entries = ((ListNode) leftNode).getEntries();

            Node result;

            evaluator.pushTrace(indexNode, "index ["+index+"]");
            if (index < 0 || index >= entries.size()) {
                result = NullNode.INSTANCE;
            } else {
                result = entries.get((int) index).evaluate(evaluator);
            }
            evaluator.popTrace();

            return result;
        }

        // Filter list
        if (leftNode instanceof ListNode && indexNode instanceof ClosureNode) {
            ClosureNode closure = (ClosureNode) indexNode;
            List<Node> entries = ((ListNode) leftNode).getEntries();
            List<Node> newEntries = new ArrayList<>();

            // Simply render the index function, since closures cannot be rendered
            evaluator.pushTrace(this.index, "[filter]");
            for (Node entry : entries) {
                CallNode callExpression = new CallNode(closure, entry);
                Node reduced = callExpression.evaluate(evaluator);
                if (!(reduced instanceof BooleanNode)) {
                    throw new EvaluationException("Filter function must return a boolean but found " + reduced);
                }

                if (((BooleanNode) reduced).getValue()) {
                    newEntries.add(entry.evaluate(evaluator));
                }
            }
            evaluator.popTrace();

            return new ListNode(newEntries);
        }

        // Index map
        if (leftNode instanceof MapNode && indexNode instanceof StringNode) {
            String key = ((StringNode) indexNode).getValue();
            Map<String, Node> entries = ((MapNode) leftNode).getEntries();

            Node result;

            evaluator.pushTrace(indexNode, "member [\""+key+"\"]");
            if (!entries.containsKey(key)) {
                result = NullNode.INSTANCE;
            } else {
                result = entries.get(key).evaluate(evaluator);
            }
            evaluator.popTrace();

            return result;
        }

        throw new EvaluationException("Can't index " + leftNode + " with " + indexNode);
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        left.render(renderer, builder, currentIndentationMultiplier + 1);
        builder.append('[');
        index.render(renderer, builder, currentIndentationMultiplier + 1);
        builder.append(']');
    }
}
