package org.quiltmc.chasm.lang.op;

public interface ListExpression extends AddableExpression, IterableExpression, IndexableExpression {
    int getLength();
}
