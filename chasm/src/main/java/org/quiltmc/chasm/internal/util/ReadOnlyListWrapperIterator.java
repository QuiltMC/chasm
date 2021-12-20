/**
 *
 */
package org.quiltmc.chasm.internal.util;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ReadOnlyListWrapperIterator<E, L extends List<E>> implements Iterator<E> {
    protected final L list;
    protected int nextIndex = 0;

    public ReadOnlyListWrapperIterator(L list) {
        this.list = list;
    }

    public ReadOnlyListWrapperIterator(L list, int nextIndex) {
        this.list = list;
        if (nextIndex < 0) {
            nextIndex = 0;
        }
        this.nextIndex = nextIndex;
    }

    @Override
    public boolean hasNext() {
        return nextIndex < list.size();
    }

    @Override
    public E next() {
        if (nextIndex >= list.size()) {
            throw new NoSuchElementException("no entry at list index " + nextIndex);
        }
        final E element = list.get(nextIndex);
        ++nextIndex;
        return element;
    }
}