package org.quiltmc.chasm.lang.op;

import java.util.Iterator;

import org.quiltmc.chasm.lang.ast.Expression;

public interface Iterable {
    Iterator<Expression> iterate();
}
