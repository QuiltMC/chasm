package org.quiltmc.chasm.lang.api.ast;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.Renderer;

public class ListNode extends Node {
    private List<Node> entries;

    public ListNode(List<Node> entries) {
        this.entries = entries;
    }

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

        for (int i = 0; i < entries.size(); i++) {
            Node entry = entries.get(i);

            evaluator.pushTrace(entry, "list entry ["+i+"]");
            newEntries.add(entry.evaluate(evaluator));
            evaluator.popTrace();
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
}
