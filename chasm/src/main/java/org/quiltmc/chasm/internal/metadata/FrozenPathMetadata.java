/**
 *
 */
package org.quiltmc.chasm.internal.metadata;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 *
 */
public class FrozenPathMetadata implements FrozenMetadata, List<PathEntry> {
    private static final String FROZEN_PATH_METADATA_IS_IMMUTABLE = "FrozenPathMetadata is immutable.";

    /**
     *
     */
    private final PathEntry[] frozenPath;

    /**
     * @param pathMetadata
     */
    public FrozenPathMetadata(List<PathEntry> pathMetadata) {
        frozenPath = pathMetadata.toArray(PathEntry[]::new);
    }

    private FrozenPathMetadata(PathEntry[] path) {
        frozenPath = path;
    }

    @Override
    public Metadata thaw() {
        return new PathMetadata(this);
    }

    @Override
    public int size() {
        return frozenPath.length;
    }

    @Override
    public boolean isEmpty() {
        return frozenPath.length == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    private static class FrozenPathIterator implements Iterator<PathEntry> {
        final PathEntry[] path;
        int nextIndex;

        protected FrozenPathIterator(PathEntry[] path) {
            this.path = path;
            nextIndex = 0;
        }

        @Override
        public boolean hasNext() {
            return nextIndex < path.length;
        }
        @Override
        public PathEntry next() {
            if (nextIndex >= path.length) {
                throw new NoSuchElementException("Exceeded FrozenPathMetadata iterator length.");
            }
            PathEntry nextEntry = path[nextIndex];
            nextIndex++;
            return nextEntry;
        }
    }
    @Override
    public Iterator<PathEntry> iterator() {
        return new FrozenPathIterator(frozenPath);
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(frozenPath, frozenPath.length);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length == frozenPath.length) {
            System.arraycopy(frozenPath, 0, a, 0, frozenPath.length);
            return a;
        }
        return (T[]) Arrays.copyOf(frozenPath, frozenPath.length);
    }

    @Override
    public boolean add(PathEntry e) {
        throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Collection<?> t = this;
        if (c.size() > t.size()) {
            Collection<?> o = t;
            t = c;
            c = o;
        }
        for (Object o : t) {
            if (!c.contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends PathEntry> c) {
        throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
    }

    @Override
    public boolean addAll(int index, Collection<? extends PathEntry> c) {
        throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
    }

    @Override
    public PathEntry get(int index) {
        return frozenPath[index];
    }

    @Override
    public PathEntry set(int index, PathEntry element) {
        throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
    }

    @Override
    public void add(int index, PathEntry element) {
        throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
    }

    @Override
    public PathEntry remove(int index) {
        throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
    }

    @Override
    public int indexOf(Object o) {
        if (!(o instanceof PathEntry)) {
            return -1;
        }
        for (int i = 0; i < frozenPath.length; ++i) {
            if (frozenPath[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        if (!(o instanceof PathEntry)) {
            return -1;
        }
        for (int i = frozenPath.length - 1; i >= 0; --i) {
            if (frozenPath[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    private static final class FrozenPathListIterator extends FrozenPathIterator implements ListIterator<PathEntry> {
        /**
         * @param path
         */
        protected FrozenPathListIterator(PathEntry[] path) {
            super(path);
        }

        /**
         * @param frozenPath
         * @param index
         */
        public FrozenPathListIterator(PathEntry[] frozenPath, int index) {
            super(frozenPath);
            nextIndex = index;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public PathEntry previous() {
            --nextIndex;
            return path[nextIndex];
        }

        @Override
        public int nextIndex() {
            if (nextIndex >= path.length) {
                return path.length;
            }
            return nextIndex++;
        }

        @Override
        public int previousIndex() {
            if (nextIndex <= 0) {
                return -1;
            }
            return --nextIndex;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
        }
        @Override
        public void set(PathEntry e) {
            throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
        }
        @Override
        public void add(PathEntry e) {
            throw new UnsupportedOperationException(FROZEN_PATH_METADATA_IS_IMMUTABLE);
        }
    }
    @Override
    public ListIterator<PathEntry> listIterator() {
        return new FrozenPathListIterator(frozenPath);
    }

    @Override
    public ListIterator<PathEntry> listIterator(int index) {
        return new FrozenPathListIterator(frozenPath, index);
    }

    @Override
    public List<PathEntry> subList(int fromIndex, int toIndex) {
        return new FrozenPathMetadata(Arrays.copyOfRange(frozenPath, fromIndex, toIndex));
    }
}
