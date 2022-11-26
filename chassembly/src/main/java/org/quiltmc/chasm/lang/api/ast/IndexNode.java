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

/**
 * An index expression, e.g. {@code foo[bar]}.
 */
public class IndexNode extends Node {
    private Node left;
    private Node index;

    /**
     * Creates an index expression.
     *
     * @see Ast#index(Node, Node)
     */
    public IndexNode(Node left, Node index) {
        this.left = left;
        this.index = index;
    }

    /**
     * Gets the subject of this index expression, i.e. the object being indexed into.
     */
    public Node getLeft() {
        return left;
    }

    /**
     * Sets the subject of this index expression, i.e. the object being indexed into.
     */
    public void setLeft(Node node) {
        this.left = node;
    }

    /**
     * Gets the index.
     */
    public Node getIndex() {
        return index;
    }

    /**
     * Gets the index.
     */
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
        Node indexNode = this.index.evaluate(evaluator);

        // Index list
        if (leftNode instanceof ListNode && indexNode instanceof IntegerNode) {
            long index = ((IntegerNode) indexNode).getValue();
            List<Node> entries = ((ListNode) leftNode).getEntries();

            if (index < 0 || index >= entries.size()) {
                return NullNode.INSTANCE;
            }

            return entries.get((int) index).evaluate(evaluator);
        }

        // Filter list
        if (leftNode instanceof ListNode && indexNode instanceof ClosureNode) {
            ClosureNode closure = (ClosureNode) indexNode;
            List<Node> entries = ((ListNode) leftNode).getEntries();
            List<Node> newEntries = new ArrayList<>();

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

            return new ListNode(newEntries);
        }

        // Index map
        if (leftNode instanceof MapNode && indexNode instanceof StringNode) {
            String key = ((StringNode) indexNode).getValue();
            Map<String, Node> entries = ((MapNode) leftNode).getEntries();

            if (!entries.containsKey(key)) {
                return NullNode.INSTANCE;
            }

            return entries.get(key).evaluate(evaluator);
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
