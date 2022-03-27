package org.quiltmc.chasm.lang.op;

import org.antlr.v4.runtime.tree.ParseTree;

public interface ComparableExpression extends EquatableExpression {
    boolean canCompare(Expression expression);

    Expression lessThan(ParseTree tree, Expression expression);

    Expression lessThanOrEqual(ParseTree tree, Expression expression);

    Expression greaterThan(ParseTree tree, Expression expression);

    Expression greaterThanOrEqual(ParseTree tree, Expression expression);
}
