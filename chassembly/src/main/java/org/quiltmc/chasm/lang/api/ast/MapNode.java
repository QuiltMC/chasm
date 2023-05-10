package org.quiltmc.chasm.lang.api.ast;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.Assert;
import org.quiltmc.chasm.lang.internal.render.RenderUtil;
import org.quiltmc.chasm.lang.internal.render.Renderer;

/**
 * A map expression, for map creation syntax, e.g. {@code {foo: bar}}.
 */
public class MapNode extends Node {
    private final Map<String, Node> entries;

    /**
     * Creates a map expression.
     *
     * @see Ast#map()
     */
    public MapNode(Map<String, Node> entries) {
        Assert.check(entries.getClass() != HashMap.class,
                "HashMap given to MapNode, order will be impure!");
        this.entries = entries;
    }

    /**
     * Gets the entries of this map expression.
     */
    public Map<String, Node> getEntries() {
        return entries;
    }

    /**
     * Gets the value associated with the given key, or null if no value was associated.
     */
    @Nullable
    public Node get(String key) {
        return this.entries.get(key);
    }

    /**
     * Sets the value associated with the given key, returning the previous value if present.
     */
    @Nullable
    public Node put(String key, Node value) {
        return this.entries.put(key, value);
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append('{');
        List<Map.Entry<String, Node>> list = new LinkedList<>();
        entries.entrySet().forEach(list::add);
        for (int i = 0; i < list.size(); i++) {
            renderer.indent(builder, currentIndentationMultiplier);
            String key = list.get(i).getKey();
            builder.append(RenderUtil.quotifyIdentifierIfNeeded(key, '"'));
            builder.append(": ");
            list.get(i).getValue().render(renderer, builder, currentIndentationMultiplier + 1);
            if (i < entries.size() - 1 || renderer.hasTrailingCommas()) {
                builder.append(", ");
            }
        }
        if (list.size() > 0) {
            renderer.indent(builder, currentIndentationMultiplier - 1);
        }
        builder.append('}');
    }

    @Override
    @ApiStatus.OverrideOnly
    public void resolve(Resolver resolver) {
        resolver.enterMap(this);
        entries.values().forEach(node -> node.resolve(resolver));
        resolver.exitMap();
    }

    @Override
    @ApiStatus.OverrideOnly
    public Node evaluate(Evaluator evaluator) {
        Map<String, Node> newEntries = new LinkedHashMap<>();

        for (Map.Entry<String, Node> entry : entries.entrySet()) {
            newEntries.put(entry.getKey(), entry.getValue().evaluate(evaluator));
        }

        if (newEntries.equals(entries)) {
            return this;
        }

        return new MapNode(newEntries);
    }

    @Override
    public String typeName() {
        return "map";
    }

    /**
     * A builder for map nodes.
     *
     * @see Ast#map()
     */
    public static final class Builder {
        private final Map<String, Node> entries = new LinkedHashMap<>();

        Builder() {
        }

        /**
         * Adds an entry to this map node.
         * Supported values are null, boxed primitives, strings, maps for map nodes, iterables for list nodes,
         * builders for map and list nodes, and nodes.
         */
        public Builder put(String key, @Nullable Object value) {
            entries.put(key, Ast.objectToNode(value));
            return this;
        }

        /**
         * Adds the given entries to this map node.
         * Supported values are the same as in {@linkplain #put(String, Object)}.
         */
        public Builder putAll(Map<String, ?> values) {
            values.forEach((key, value) -> entries.put(key, Ast.objectToNode(value)));
            return this;
        }

        /**
         * Creates the map node.
         */
        public MapNode build() {
            return new MapNode(entries);
        }
    }
}
