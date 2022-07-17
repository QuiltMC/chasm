package org.quiltmc.chasm.lang.api.ast;

import java.util.ArrayList;
import java.util.List;

import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class ListExpression extends Expression {
    private final List<Expression> entries;

    public ListExpression(List<Expression> entries) {
        this.entries = entries;
    }

    public List<Expression> getEntries() {
        return entries;
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

    @Override
    public ListExpression copy() {
        List<Expression> newEntries = new ArrayList<>();

        for (Expression entry : entries) {
            newEntries.add(entry.copy());
        }

        return new ListExpression(newEntries);
    }
}
