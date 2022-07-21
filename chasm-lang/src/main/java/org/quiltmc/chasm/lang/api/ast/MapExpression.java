package org.quiltmc.chasm.lang.api.ast;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.quiltmc.chasm.lang.internal.render.RendererConfig;

public class MapExpression extends Expression {
    private final Map<String, Expression> entries;

    public MapExpression(Map<String, Expression> entries) {
        this.entries = entries;
    }

    public Map<String, Expression> getEntries() {
        return entries;
    }

    @Override
    public void render(RendererConfig config, StringBuilder builder, int currentIndentationMultiplier) {
        builder.append('{');
        List<Map.Entry<String, Expression>> list = new LinkedList<>();
        entries.entrySet().forEach(list::add);
        for (int i = 0; i < list.size(); i++) {
            Expression.indent(config, builder, currentIndentationMultiplier);
            builder.append(list.get(i).getKey()).append(": ");
            list.get(i).getValue().render(config, builder, currentIndentationMultiplier + 1);
            if (i < entries.size() - 1 || config.trailingCommas()) {
                builder.append(", ");
            }
        }
        if (list.size() > 0) {
            Expression.indent(config, builder, currentIndentationMultiplier - 1);
        }
        builder.append('}');
    }

    @Override
    public MapExpression copy() {
        Map<String, Expression> newEntries = new LinkedHashMap<>();

        for (Map.Entry<String, Expression> entry : entries.entrySet()) {
            newEntries.put(entry.getKey(), entry.getValue().copy());
        }

        return new MapExpression(newEntries);
    }
}
