package org.quiltmc.chasm.lang.op;

import org.quiltmc.chasm.lang.ast.Expression;

public interface Multiplicable {
    boolean canMultiply(Expression expression);

    Expression multiply(Expression expression);
}
