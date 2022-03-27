package org.quiltmc.chasm.lang.ast;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.Cache;
import org.quiltmc.chasm.lang.Scope;
import org.quiltmc.chasm.lang.ScopeStack;
import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.MapExpression;

public abstract class AbstractMapExpression extends AbstractExpression implements MapExpression {
    public AbstractMapExpression(ParseTree tree) {
        super(tree);
    }

    public abstract Expression get(String key);

    public abstract Collection<String> getKeys();

    @Override
    public Expression reduce(Cache cache) {
        return this;
    }

    @Override
    public Expression resolve(ScopeStack scope) {
        Map<String, Expression> resolvedEntries = new LinkedHashMap<>();

        scope.push(Scope.map(resolvedEntries));
        for (String key : getKeys()) {
            resolvedEntries.put(key, get(key).resolve(scope));
        }
        scope.pop();

        return new SimpleMapExpression(getParseTree(), resolvedEntries);
    }

    @Override
    public boolean canIndex(Expression expression) {
        return expression instanceof StringExpression;
    }

    @Override
    public Expression index(ParseTree tree, Expression expression) {
        Expression result = get(((StringExpression) expression).getValue());
        if (result == null) {
            return new NullExpression(tree);
        } else {
            return result;
        }
    }
}
