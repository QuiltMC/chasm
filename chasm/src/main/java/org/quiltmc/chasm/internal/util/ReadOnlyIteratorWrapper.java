/**
 *
 */
package org.quiltmc.chasm.internal.util;

import java.util.Collections;
import java.util.Iterator;

public class ReadOnlyIteratorWrapper<E> implements Iterator<E> {
    private Iterator<E> iter;

    public ReadOnlyIteratorWrapper() {
        this.iter = Collections.emptyIterator();
    }

    public ReadOnlyIteratorWrapper(Iterator<E> iter) {
        this.iter = iter;
    }

    @Override
    public boolean hasNext() {
        return this.iter.hasNext();
    }

    @Override
    public E next() {
        return this.iter.next();
    }
}
