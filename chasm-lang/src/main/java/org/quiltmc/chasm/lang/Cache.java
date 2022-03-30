package org.quiltmc.chasm.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.quiltmc.chasm.lang.op.Expression;
import org.quiltmc.chasm.lang.op.FunctionExpression;

public class Cache {
    private final Map<Expression, Expression> expressionCache;
    private final Map<CallCacheKey, Expression> callCache;

    public Cache() {
        expressionCache = new HashMap<>();
        expressionCache.put(null, null);
        callCache = new HashMap<>();
    }

    public Expression callCached(FunctionExpression function, Expression argument) {
        CallCacheKey cacheKey = new CallCacheKey(function, argument);
        if (callCache.containsKey(cacheKey)) {
            return callCache.get(cacheKey);
        } else {
            Expression result = function.call(argument);
            callCache.put(cacheKey, result);
            return result;
        }
    }

    public Expression reduceCached(Expression expression) {
        if (expressionCache.containsKey(expression)) {
            return expressionCache.get(expression);
        } else {
            Expression reduced = expression.reduce(this);
            expressionCache.put(expression, reduced);
            return reduced;
        }
    }

    static class CallCacheKey {
        private final FunctionExpression function;
        private final Expression argument;

        public CallCacheKey(FunctionExpression function, Expression argument) {
            this.function = function;
            this.argument = argument;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CallCacheKey that = (CallCacheKey) o;
            return Objects.equals(function, that.function) && Objects.equals(argument, that.argument);
        }

        @Override
        public int hashCode() {
            return Objects.hash(function, argument);
        }
    }
}
