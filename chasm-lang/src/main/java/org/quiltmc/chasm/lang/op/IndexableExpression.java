package org.quiltmc.chasm.lang.op;

import org.antlr.v4.runtime.tree.ParseTree;

public interface IndexableExpression extends Expression {
    boolean canIndex(Expression expression);

    Expression index(ParseTree tree, Expression expression);
}
