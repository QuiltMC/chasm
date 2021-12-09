/**
 *
 */
package org.quiltmc.chasm.internal.tree.frozencollection;

import java.util.Collection;
import java.util.function.Predicate;

public abstract class AbstractImmutableCollection<T> implements Collection<T> {
    protected String immutableErrorString() {
        return this.getClass().getTypeName() + "s are immutable.";
    }

    @Override
    public final boolean add(T e) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public final boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public final void clear() {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public final boolean remove(Object o) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public final boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public final boolean removeIf(Predicate<? super T> filter) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public final boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(immutableErrorString());
    }
}