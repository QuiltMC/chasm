package org.quiltmc.chasm.lang.ast;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.op.Expression;

public class SimpleMapExpression extends AbstractMapExpression {
    private final Map<String, Expression> entries;

    public SimpleMapExpression(ParseTree tree, Map<String, Expression> entries) {
        super(tree);
        this.entries = Collections.unmodifiableMap(entries);
    }

    @Override
    public Expression get(String index) {
        return entries.get(index);
    }

    @Override
    public Collection<String> getKeys() {
        return entries.keySet();
    }

    @Override
    public String toString() {
        return entries.toString();
    }
}
