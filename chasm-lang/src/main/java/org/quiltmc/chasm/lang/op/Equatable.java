package org.quiltmc.chasm.lang.op;

import org.quiltmc.chasm.lang.ast.Expression;

public interface Equatable {
    boolean canEquate(Expression expression);

    Expression equate(Expression expression);
}
