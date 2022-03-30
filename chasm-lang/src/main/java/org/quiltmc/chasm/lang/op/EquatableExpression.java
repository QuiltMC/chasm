package org.quiltmc.chasm.lang.op;

import org.antlr.v4.runtime.tree.ParseTree;
import org.quiltmc.chasm.lang.ast.ConstantBooleanExpression;

public interface EquatableExpression extends Expression {
    boolean canEquate(Expression expression);

    ConstantBooleanExpression equate(ParseTree tree, Expression expression);
}
