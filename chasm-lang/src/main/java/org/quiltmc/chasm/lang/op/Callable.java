package org.quiltmc.chasm.lang.op;

import org.quiltmc.chasm.lang.ast.Expression;

public interface Callable {
    boolean canCall(Expression expression);

    Expression call(Expression expression);
}
