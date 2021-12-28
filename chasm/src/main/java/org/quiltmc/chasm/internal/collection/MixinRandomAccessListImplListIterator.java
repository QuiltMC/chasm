/**
 *
 */
package org.quiltmc.chasm.internal.collection;

import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 *
 */
public class MixinRandomAccessListImplListIterator<E> extends MixinRandomAccessListImplIterator<E>
        implements ListIterator<E> {

    /**
     * @param list
     */
    public MixinRandomAccessListImplListIterator(SimpleRandomAccessList<E> list) {
        super(list);
    }

    /**
     * @param list
     * @param i
     */
    public MixinRandomAccessListImplListIterator(SimpleRandomAccessList<E> list, int i) {
        super(list, i);
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
        return this.list.get(this.nextIndex);
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
