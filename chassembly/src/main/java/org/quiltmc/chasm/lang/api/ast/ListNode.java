package org.quiltmc.chasm.lang.api.ast;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * A list expression, for list creation syntax, e.g. {@code [foo, bar, baz]}.
 */
public class ListNode extends Node {
    private final List<Node> entries;

    /**
     * Creates a list expression.
     *
     * @see Ast#list()
     */
    public ListNode(List<Node> entries) {
        this.entries = entries;
    }

    /**
     * Gets the entries of this list expression.
     */
    public List<Node> getEntries() {
        return entries;
    }

    @Override
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
        entries.forEach(node -> node.resolve(resolver));
    }

    @Override
    @ApiStatus.OverrideOnly
    public Node evaluate(Evaluator evaluator) {
        List<Node> newEntries = new ArrayList<>();

        for (Node entry : entries) {
            newEntries.add(entry.evaluate(evaluator));
        }

        if (newEntries.equals(entries)) {
            return this;
        }

        return new ListNode(newEntries);
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append("[");
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).render(renderer, builder, currentIndentationMultiplier + 1);
            if (i < entries.size() - 1 || renderer.hasTrailingCommas()) {
                builder.append(',');
            }
        }
        builder.append("]");
    }

    /**
     * A builder for list nodes.
     *
     * @see Ast#list()
     */
    public static final class Builder {
        private final List<Node> entries = new ArrayList<>();

        Builder() {
        }

        /**
         * Adds an object to this list node.
         * Supported values are null, boxed primitives, strings, maps for map nodes, iterables for list nodes,
         * builders for map and list nodes, and nodes.
         */
        public Builder add(@Nullable Object value) {
            entries.add(Ast.objectToNode(value));
            return this;
        }

        /**
         * Adds a sequence of objects to this list node.
         * Supported values are the same as in {@linkplain #add(Object)}.
         */
        public Builder addAll(@Nullable Object @NotNull ... values) {
            for (Object value : values) {
                entries.add(Ast.objectToNode(value));
            }
            return this;
        }

        /**
         * Adds a sequence of objects to this list node.
         * Supported values are the same as in {@linkplain #add(Object)}.
         */
        public Builder addAll(@NotNull Iterable<?> values) {
            for (Object value : values) {
                entries.add(Ast.objectToNode(value));
            }
            return this;
        }

        /**
         * Creates the list node.
         */
        public ListNode build() {
            return new ListNode(entries);
        }
    }
}
