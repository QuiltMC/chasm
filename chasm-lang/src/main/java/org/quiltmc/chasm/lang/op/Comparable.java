package org.quiltmc.chasm.lang.op;

import org.quiltmc.chasm.lang.ast.Expression;

public interface Comparable extends Equatable {
    boolean canCompare(Expression expression);

    Expression lessThan(Expression expression);

    Expression lessThanOrEqual(Expression expression);

    Expression greaterThan(Expression expression);

    Expression greaterThanOrEqual(Expression expression);
}
