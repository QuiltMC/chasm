package org.quiltmc.chasm.lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.quiltmc.chasm.lang.ast.Expression;

public class ReductionContext {
    Map<Expression, Expression> expressions = new HashMap<>();

    public Expression reduce(Expression expression) {
        // Return reduced expression if it was reduced before
        if (expressions.containsKey(expression)) {
            return expressions.get(expression);
        }

        // Temporarily cache the original expression to prevent infinite recursion
        expressions.put(expression, expression);

        // The Expression.reduce() method is only called once for each expression
        Expression reduced = expression.reduce(this);

        // Store the reduced expression and return it
        expressions.put(expression, reduced);
        return reduced;
    }
}
