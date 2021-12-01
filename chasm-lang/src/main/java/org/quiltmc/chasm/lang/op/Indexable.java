package org.quiltmc.chasm.lang.op;

import org.quiltmc.chasm.lang.ast.Expression;

public interface Indexable {
    boolean canIndex(Expression expression);

    Expression index(Expression expression);
}
