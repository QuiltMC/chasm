package org.quiltmc.chasm.lang.op;

import org.antlr.v4.runtime.tree.ParseTree;

public interface AddableExpression extends Expression {
    boolean canAdd(Expression expression);

    Expression add(ParseTree tree, Expression expression);
}
