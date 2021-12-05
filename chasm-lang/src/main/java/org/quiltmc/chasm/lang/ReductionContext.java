package org.quiltmc.chasm.lang;

import java.util.HashMap;
import java.util.Map;

import org.quiltmc.chasm.lang.ast.Expression;
import org.quiltmc.chasm.lang.ast.ListExpression;
import org.quiltmc.chasm.lang.ast.MapExpression;

public class ReductionContext {
    Map<Expression, Expression> expressions = new HashMap<>();

    public Expression reduce(Expression expression) {
        // Return reduced expression if it was reduced before
        if (expressions.containsKey(expression)) {
            return expressions.get(expression);
        }

        // Only visit maps and lists once
        if (expression instanceof MapExpression || expression instanceof ListExpression) {
            // Temporarily cache the original expression to prevent infinite recursion
            expressions.put(expression, expression);
        }

        // Store the reduced expression and return it
        Expression reduced = expression.reduce(this);
        expressions.put(expression, reduced);
        return reduced;
    }
}
