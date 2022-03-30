package org.quiltmc.chasm.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.quiltmc.chasm.lang.ast.AbstractListExpression;
import org.quiltmc.chasm.lang.ast.AbstractMapExpression;
import org.quiltmc.chasm.lang.ast.SimpleListExpression;
import org.quiltmc.chasm.lang.ast.SimpleMapExpression;
import org.quiltmc.chasm.lang.op.Expression;

public class Evaluator {
    private final Cache cache = new Cache();
    private final ScopeStack scope = new ScopeStack();

    public ScopeStack getScope() {
        return scope;
    }

    public Expression resolve(Expression expression) {
        // Allow the expression to refer to its root via "this"
        SelfScope selfScope = new SelfScope();

        // Resolve the expression with the self scope
        scope.push(selfScope);
        Expression resolved = expression.resolve(scope);
        scope.pop();

        // Point the self scope to the resolved expression
        selfScope.set(resolved);

        // Return the resolved expression
        return resolved;
    }

    public Expression reduce(Expression expression) {
        return cache.reduceCached(expression);
    }

    public Expression reduceRecursive(Expression expression) {
        Expression reduced = reduce(expression);

        if (reduced instanceof AbstractListExpression) {
            AbstractListExpression list = (AbstractListExpression) reduced;
            List<Expression> newEntries = new ArrayList<>();
            for (Expression entry : list) {
                newEntries.add(reduceRecursive(entry));
            }
            return new SimpleListExpression(reduced.getParseTree(), newEntries);
        } else if (reduced instanceof AbstractMapExpression) {
            AbstractMapExpression map = (AbstractMapExpression) reduced;
            Map<String, Expression> newEntries = new LinkedHashMap<>();
            for (String key : map.getKeys()) {
                newEntries.put(key, reduceRecursive(map.get(key)));
            }
            return new SimpleMapExpression(reduced.getParseTree(), newEntries);
        } else {
            return reduced;
        }
    }

    private static class SelfScope implements Scope {
        private Expression value = null;

        public void set(Expression value) {
            this.value = value;
        }

        @Override
        public boolean contains(String identifier) {
            return identifier.equals("this");
        }

        @Override
        public Expression get(String identifier) {
            return contains(identifier) ? value : null;
        }
    }
}
