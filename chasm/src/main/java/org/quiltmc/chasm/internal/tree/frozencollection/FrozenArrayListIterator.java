/**
 * 
 */
package org.quiltmc.chasm.internal.tree.frozencollection;

import java.util.ListIterator;
import java.util.NoSuchElementException;

final class FrozenArrayListIterator<T> extends FrozenArrayIterator<T> implements ListIterator<T> {
    private static final String FROZEN_ARRAY_LIST_ITERATORS_ARE_IMMUTABLE = "FrozenArrayListIterators are immutable.";

    protected FrozenArrayListIterator(T[] elements) {
        super(elements);
    }

    protected FrozenArrayListIterator(T[] elements, int index) {
        super(elements);
        nextIndex = index;
    }

    @Override
    public boolean hasPrevious() {
        return nextIndex > 0;
    }

    @Override
    public T previous() {
        if (nextIndex <= 0) {
            throw new NoSuchElementException();
        }
        --nextIndex;
        return elements[nextIndex];
    }

    @Override
    public int nextIndex() {
        return nextIndex;
    }

    @Override
    public int previousIndex() {
        return nextIndex - 1;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException(FROZEN_ARRAY_LIST_ITERATORS_ARE_IMMUTABLE);
    }

    @Override
    public void set(T e) {
        throw new UnsupportedOperationException(FROZEN_ARRAY_LIST_ITERATORS_ARE_IMMUTABLE);
    }

    @Override
    public void add(T e) {
        throw new UnsupportedOperationException(FROZEN_ARRAY_LIST_ITERATORS_ARE_IMMUTABLE);
    }
}