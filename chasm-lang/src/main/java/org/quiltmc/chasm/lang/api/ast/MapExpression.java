package org.quiltmc.chasm.lang.api.ast;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class MapExpression extends Expression {
    private final Map<String, Expression> entries;

    public MapExpression(Map<String, Expression> entries) {
        this.entries = entries;
    }

    public Map<String, Expression> getEntries() {
        return entries;
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
