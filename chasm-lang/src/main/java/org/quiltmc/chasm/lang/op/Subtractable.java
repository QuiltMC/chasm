package org.quiltmc.chasm.lang.op;

import org.quiltmc.chasm.lang.ast.Expression;

public interface Subtractable {
    boolean canSubtract(Expression expression);

    Expression subtract(Expression expression);
}
