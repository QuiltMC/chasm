/**
 *
 */
package org.quiltmc.chasm.internal.util;

import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class ListWrapperListIterator<E, L extends List<E>> extends ReadOnlyListWrapperIterator<E, L>
        implements ListIterator<E> {


    public ListWrapperListIterator(L list) {
        super(list);
    }

    public ListWrapperListIterator(L list, int nextIndex) {
        super(list, nextIndex);
    }

    @Override
    public boolean hasPrevious() {
        return this.nextIndex > 0;
    }

    @Override
    public E previous() {
        if (this.nextIndex <= 0) {
            throw new NoSuchElementException();
        }
        --this.nextIndex;
        return this.list.get(nextIndex);
    }

    @Override
    public int nextIndex() {
        return this.nextIndex;
    }

    @Override
    public int previousIndex() {
        return this.nextIndex - 1;
    }

    @Override
    public void remove() {
        this.list.remove(this.previousIndex());
    }

    @Override
    public void set(E e) {
        this.list.set(this.previousIndex(), e);
    }

    @Override
    public void add(E e) {
        this.list.add(this.nextIndex, e);
    }

}