/**
 *
 */
package org.quiltmc.chasm.internal.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

class MixinRandomAccessListImplIterator<E> implements Iterator<E> {
    protected SimpleRandomAccessList<E> list;
    protected int nextIndex;

    MixinRandomAccessListImplIterator(SimpleRandomAccessList<E> list) {
        this.list = list;
        this.nextIndex = 0;
    }

    protected MixinRandomAccessListImplIterator(SimpleRandomAccessList<E> list, int i) {
        if (list.size() < i || i < 0) {
            throw new IndexOutOfBoundsException(i);
        }
        this.list = list;
        this.nextIndex = i;
    }

    @Override
    public boolean hasNext() {
        return this.nextIndex < this.list.size();
    }

    @Override
    public E next() {
        if (this.nextIndex >= this.list.size()) {
            throw new NoSuchElementException();
        }
        E element = this.list.get(nextIndex);
        ++nextIndex;
        return element;
    }
}