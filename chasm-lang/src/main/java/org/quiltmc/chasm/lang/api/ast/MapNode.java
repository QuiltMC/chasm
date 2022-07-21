package org.quiltmc.chasm.lang.api.ast;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class MapNode extends Node {
    private final Map<String, Node> entries;

    public MapNode(Map<String, Node> entries) {
        this.entries = entries;
    }

    public Map<String, Node> getEntries() {
        return entries;
    }

    @Override
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append('{');
        List<Map.Entry<String, Node>> list = new LinkedList<>();
        entries.entrySet().forEach(list::add);
        for (int i = 0; i < list.size(); i++) {
            Node.indent(config, builder, currentIndentationMultiplier);
            builder.append(list.get(i).getKey()).append(": ");
            list.get(i).getValue().render(config, builder, currentIndentationMultiplier + 1);
            if (i < entries.size() - 1 || config.trailingCommas()) {
                builder.append(", ");
            }
        }
        if (list.size() > 0) {
            Node.indent(config, builder, currentIndentationMultiplier - 1);
        }
        builder.append('}');
    }

    public MapNode copy() {
        Map<String, Node> newEntries = new LinkedHashMap<>();

        for (Map.Entry<String, Node> entry : entries.entrySet()) {
            newEntries.put(entry.getKey(), entry.getValue().copy());
        }

        return new MapNode(newEntries);
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

        return new MapNode(newEntries);
    }
}
