package org.quiltmc.chasm.lang.api.ast;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.OperatorPriority;
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
    public void render(Renderer renderer, StringBuilder builder, int indentation, OperatorPriority minPriority) {
        boolean needsBrackets = !OperatorPriority.ARGUMENT_PRIMARY.allowedFor(minPriority);
        if (needsBrackets) {
            builder.append('(');
        }
        builder.append('{');
        List<Map.Entry<String, Node>> list = new LinkedList<>();
        entries.entrySet().forEach(list::add);
        for (int i = 0; i < list.size(); i++) {
            renderer.indent(builder, indentation);
            builder.append(list.get(i).getKey()).append(": ");
            list.get(i).getValue().render(renderer, builder, indentation + 1, OperatorPriority.ANY);
            if (i < entries.size() - 1 || renderer.hasTrailingCommas()) {
                builder.append(",");
            }
        }
        if (list.size() > 0) {
            renderer.indent(builder, indentation - 1);
        }
        builder.append('}');
        if (needsBrackets) {
            builder.append(')');
        }
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
