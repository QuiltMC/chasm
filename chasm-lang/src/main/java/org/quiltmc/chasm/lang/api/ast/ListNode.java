package org.quiltmc.chasm.lang.api.ast;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class ListNode extends Node {
    private List<Node> entries;

    public ListNode(List<Node> entries) {
        this.entries = entries;
    }

    public List<Node> getEntries() {
        return entries;
    }

    @Override
    public ListNode copy() {
        List<Node> newEntries = new ArrayList<>();

        for (Node entry : entries) {
            newEntries.add(entry.copy());
        }

        return new ListNode(newEntries);
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

        return new ListNode(newEntries);
    }

    @Override
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append("[");
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).render(config, builder, currentIndentationMultiplier + 1);
            if (i < entries.size() - 1 || config.trailingCommas()) {
                builder.append(',');
            }
        }
        builder.append("]");
    }
}
