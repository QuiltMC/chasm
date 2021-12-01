package org.quiltmc.chasm.lang.op;

import org.quiltmc.chasm.lang.ast.Expression;

public interface Addable {
    boolean canAdd(Expression expression);

    Expression add(Expression expression);
}
