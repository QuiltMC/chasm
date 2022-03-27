package org.quiltmc.chasm.lang.op;

import org.antlr.v4.runtime.tree.ParseTree;

public interface Subtractable {
    boolean canSubtract(Expression expression);

    Expression subtract(ParseTree tree, Expression expression);
}
