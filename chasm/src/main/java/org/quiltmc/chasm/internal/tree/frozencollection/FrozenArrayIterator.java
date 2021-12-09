/**
 *
 */
package org.quiltmc.chasm.internal.tree.frozencollection;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class FrozenArrayIterator<T> implements Iterator<T> {
    protected int nextIndex = 0;
    protected final T[] elements;

    public FrozenArrayIterator(T[] elements) {
        this.elements = elements;
    }

    @Override
    public boolean hasNext() {
        return nextIndex < elements.length;
    }

    @Override
    public T next() {
        if (nextIndex >= elements.length) {
            throw new NoSuchElementException();
        }
        int index = nextIndex;
        nextIndex++;
        return elements[index];
    }
}