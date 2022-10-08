package org.quiltmc.chasm.lang.api.ast;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public class MapNode extends Node {
    private final Map<String, Node> entries;

    public MapNode(Map<String, Node> entries) {
        this.entries = entries;
    }

    public Map<String, Node> getEntries() {
        return entries;
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append('{');
        List<Map.Entry<String, Node>> list = new LinkedList<>();
        entries.entrySet().forEach(list::add);
        for (int i = 0; i < list.size(); i++) {
            renderer.indent(builder, currentIndentationMultiplier);
            String key = list.get(i).getKey();
            if (needsQuotes(key)) {
                builder.append('"').append(key.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            } else {
                builder.append(key);
            }
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

    private static boolean needsQuotes(String key) {
        if (key.isEmpty()) {
            return true;
        }

        if (!isValidIdentifierStartChar(key.charAt(0))) {
            return true;
        }

        for (int i = 1; i < key.length(); i++) {
            if (!isValidIdentifierChar(key.charAt(i))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isValidIdentifierStartChar(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_';
    }

    private static boolean isValidIdentifierChar(char c) {
        return isValidIdentifierStartChar(c) || (c >= '0' && c <= '9');
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
}
