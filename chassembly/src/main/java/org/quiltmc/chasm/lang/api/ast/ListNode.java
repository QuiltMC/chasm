package org.quiltmc.chasm.lang.api.ast;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.lang.api.eval.Evaluator;
import org.quiltmc.chasm.lang.api.eval.Resolver;
import org.quiltmc.chasm.lang.internal.render.OperatorPriority;
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

        for (Node entry : entries) {
            newEntries.add(entry.evaluate(evaluator));
        }

        if (newEntries.equals(entries)) {
            return this;
        }

        return new ListNode(newEntries);
    }

    @Override
    public void render(Renderer renderer, StringBuilder builder, int indentation, OperatorPriority minPriority) {
        boolean needsBrackets = !OperatorPriority.ARGUMENT_PRIMARY.allowedFor(minPriority);
        if (needsBrackets) {
            builder.append('(');
        }
        builder.append("[");
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).render(renderer, builder, indentation + 1, OperatorPriority.ANY);
            if (i < entries.size() - 1 || renderer.hasTrailingCommas()) {
                builder.append(", ");
            }
        }
        builder.append("]");
        if (needsBrackets) {
            builder.append(')');
        }
    }
}
