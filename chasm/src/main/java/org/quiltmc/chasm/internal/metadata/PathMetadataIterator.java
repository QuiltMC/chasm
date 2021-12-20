/**
 * 
 */
package org.quiltmc.chasm.internal.metadata;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.quiltmc.chasm.internal.metadata.ListPathMetadata.Entry;

class PathMetadataIterator<P extends List<Entry>> implements Iterator<Entry> {
    protected final P path;
    protected int nextIndex = 0;

    public PathMetadataIterator(P path) {
        this.path = path;
    }

    public PathMetadataIterator(P path, int nextIndex) {
        this.path = path;
        if (nextIndex < 0) {
            nextIndex = 0;
        }
        this.nextIndex = nextIndex;
    }

    @Override
    public boolean hasNext() {
        return nextIndex < path.size();
    }

    @Override
    public Entry next() {
        if (nextIndex >= path.size()) {
            throw new NoSuchElementException("no entry at path index " + nextIndex);
        }
        final Entry element = path.get(nextIndex);
        ++nextIndex;
        return element;
    }
}