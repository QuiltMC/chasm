package org.quiltmc.chasm.lang.ast;

import java.util.LinkedHashMap;
import java.util.Map;

import org.quiltmc.chasm.lang.ReductionContext;
import org.quiltmc.chasm.lang.op.Indexable;

public class MapExpression implements Expression, Indexable {
    private final Map<String, Expression> entries;

    public MapExpression(Map<String, Expression> entries) {
        this.entries = entries;
    }

    public Map<String, Expression> getEntries() {
        return entries;
    }

    @Override
    public void resolve(String identifier, Expression value) {
        for (Expression entry : entries.values()) {
            entry.resolve(identifier, value);
        }
    }

    @Override
    public MapExpression reduce(ReductionContext context) {
        Map<String, Expression> reduced = new LinkedHashMap<>();
        for (Map.Entry<String, Expression> entry : entries.entrySet()) {
            reduced.put(entry.getKey(), context.reduce(entry.getValue()));
        }
        return new MapExpression(reduced);
    }

    @Override
    public MapExpression copy() {
        Map<String, Expression> copies = new LinkedHashMap<>();

        for (Map.Entry<String, Expression> entry : entries.entrySet()) {
            copies.put(entry.getKey(), entry.getValue().copy());
        }

        return new MapExpression(copies);
    }

    public Expression get(String key) {
        return entries.getOrDefault(key, Expression.none());
    }

    @Override
    public boolean canIndex(Expression expression) {
        return expression instanceof StringExpression;
    }

    @Override
    public Expression index(Expression expression) {
        return get(((StringExpression) expression).getValue());
    }
}
