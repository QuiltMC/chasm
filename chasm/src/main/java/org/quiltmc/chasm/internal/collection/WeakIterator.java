/**
 *
 */
package org.quiltmc.chasm.internal.collection;

import java.lang.ref.Reference;
import java.util.Iterator;

final class WeakIterator<V> implements Iterator<V> {
    private final Iterator<? extends Reference<? extends V>> iter;

    WeakIterator(Iterator<? extends Reference<? extends V>> iter) {
        this.iter = iter;
    }

    @Override
    public boolean hasNext() {
        return this.iter.hasNext();
    }

    @Override
    public V next() {
        return this.iter.next().get();
    }

}
