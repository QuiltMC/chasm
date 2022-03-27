package org.quiltmc.chasm.lang.op;

import org.antlr.v4.runtime.tree.ParseTree;

public interface MultiplicableExpression extends Expression {
    boolean canMultiply(Expression expression);

    Expression multiply(ParseTree tree, Expression expression);
}
