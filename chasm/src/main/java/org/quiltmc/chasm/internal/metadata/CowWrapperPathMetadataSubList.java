/**
 *
 */
package org.quiltmc.chasm.internal.metadata;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.quiltmc.chasm.api.metadata.COWWrapperMetadataProvider;
import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.ListPathMetadata.Entry;

class CowWrapperPathMetadataSubList implements PathMetadata {
    private final CowWrapperPathMetadata wrapper;
    private int min;
    private int max;

    /**
     * @param cowWrapperPathMetadata
     * @param fromIndex
     * @param toIndex
     */
    public CowWrapperPathMetadataSubList(CowWrapperPathMetadata cowWrapperPathMetadata, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            // reverse the range
            // +1s are because its [fromIndex, toIndex)
            int tempMin = toIndex + 1;
            toIndex = fromIndex + 1;
            fromIndex = tempMin;
        } else if (toIndex > cowWrapperPathMetadata.size()) {
            toIndex = cowWrapperPathMetadata.size();
        }
        this.wrapper = cowWrapperPathMetadata;
        this.min = fromIndex;
        this.max = toIndex;
    }

    @Override
    public int size() {
        return this.max - this.min;
    }

    @Override
    public boolean isEmpty() {
        return this.max > this.min;
    }

    @Override
    public boolean contains(Object o) {
        for (int i = this.min; i < this.max; ++i) {
            if (this.wrapper.get(i).equals(o)) {
                return true;
            }
        }
        return false;
    }

    private static class PathMetadataWrapperSubListIterator implements Iterator<ListPathMetadata.Entry> {
        protected final CowWrapperPathMetadata path;
        protected int nextIndex;
        protected int max;

        public PathMetadataWrapperSubListIterator(CowWrapperPathMetadataSubList subPath) {
            this(subPath, subPath.min);
        }

        public PathMetadataWrapperSubListIterator(CowWrapperPathMetadataSubList subPath, int i) {
            this.path = subPath.wrapper;
            this.max = subPath.max;
            if (i < subPath.min || i > this.max) {
                throw new IllegalArgumentException("Bad iterator start index: " + i);
            }
            this.nextIndex = i;
        }

        @Override
        public boolean hasNext() {
            return this.nextIndex < this.max;
        }

        @Override
        public Entry next() {
            if (this.nextIndex >= this.max) {
                throw new NoSuchElementException();
            }
            return this.path.get(this.nextIndex);
        }
    }

    @Override
    public Iterator<Entry> iterator() {
        return new PathMetadataWrapperSubListIterator(this);
    }

    @Override
    public Object[] toArray() {
        Object[] objects = new Object[this.size()];
        for (int i = 0, j = this.min; j < this.max; ++i, ++j) {
            objects[i] = this.wrapper.get(j);
        }
        return objects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        Class<T> componentType = (Class<T>) a.getClass().getComponentType();
        if (a.length < this.size()) {
            a = (T[]) Array.newInstance(componentType, this.size());
        }
        for (int i = 0, j = this.min; j < this.max; ++i, ++j) {
            a[i] = componentType.cast(this.wrapper.get(j));
        }
        return a;
    }

    @Override
    public boolean add(Entry e) {
        int oldSize = this.wrapper.size();
        this.wrapper.add(this.max, e);
        if (this.wrapper.size() > oldSize) {
            ++this.max;
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(Object o) {
        if (this.wrapper.remove(o)) {
            --this.max;
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        if (c instanceof Set<?> || c.size() < this.size()) {
            for (Object o : this) {
                if (!c.contains(o)) {
                    return false;
                }
            }
        } else {
            for (Object o : c) {
                if (!this.contains(o)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends Entry> c) {
        if (this.wrapper.addAll(this.max, c)) {
            this.max += c.size();
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Entry> c) {
        index = adjustToBounds(index);
        if (this.wrapper.addAll(index, c)) {
            this.max += c.size();
            return true;
        }
        return false;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        int readIndex = this.min;
        int writeIndex = this.min;
        for (; readIndex < this.max; ++readIndex, ++writeIndex) {
            Entry r = this.wrapper.get(readIndex);
            if (c.contains(r)) {
                if (readIndex != writeIndex) {
                    this.wrapper.set(writeIndex, this.wrapper.get(readIndex));
                }
            } else {
                ++readIndex;
            }
        }
        return trimWrappedList(readIndex, writeIndex);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        int readIndex = this.min;
        int writeIndex = this.min;
        for (; readIndex < this.max; ++readIndex, ++writeIndex) {
            Entry r = this.wrapper.get(readIndex);
            if (!c.contains(r)) {
                if (readIndex != writeIndex) {
                    this.wrapper.set(writeIndex, this.wrapper.get(readIndex));
                }
            } else {
                ++readIndex;
            }
        }
        return trimWrappedList(readIndex, writeIndex);
    }

    private final boolean trimWrappedList(int readIndex, int writeIndex) {
        this.max = writeIndex;
        for (; readIndex < this.wrapper.size(); ++readIndex, ++writeIndex) {
            this.wrapper.set(writeIndex, this.wrapper.get(readIndex));
        }
        while (this.wrapper.size() > writeIndex) {
            this.wrapper.remove(this.wrapper.size() - 1);
        }
        return readIndex > writeIndex;
    }

    @Override
    public void clear() {
        int readIndex = this.max;
        int writeIndex = this.min;
        this.trimWrappedList(readIndex, writeIndex);
    }

    @Override
    public Entry get(int index) {
        index = adjustToBounds(index);
        return this.wrapper.get(index);
    }

    private int adjustToBounds(int index) {
        index += this.min;
        if (index < this.min || index > this.max) {
            throw new IndexOutOfBoundsException(index - min);
        }
        return index;
    }

    @Override
    public Entry set(int index, Entry element) {
        index = adjustToBounds(index);
        return this.wrapper.set(index, element);
    }

    @Override
    public void add(int index, Entry element) {
        index = adjustToBounds(index);
        this.wrapper.add(index, element);
    }

    @Override
    public Entry remove(int index) {
        index = adjustToBounds(index);
        return this.wrapper.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        for (int i = this.min; i < this.max; ++i) {
            if (this.wrapper.get(i).equals(o)) {
                return i - this.min;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = this.max - 1; i >= this.min; --i) {
            if (this.wrapper.get(i).equals(o)) {
                return i - this.min;
            }
        }
        return -1;
    }

    private static class PathMetadataSubListWrapperListIterator extends PathMetadataWrapperSubListIterator
            implements ListIterator<Entry> {
        private final int min;

        public PathMetadataSubListWrapperListIterator(CowWrapperPathMetadataSubList subPath) {
            super(subPath);
            this.min = subPath.min;
        }

        public PathMetadataSubListWrapperListIterator(CowWrapperPathMetadataSubList subPath, int i) {
            super(subPath, i);
            this.min = subPath.min;
        }

        @Override
        public boolean hasPrevious() {
            return this.nextIndex > 0;
        }

        @Override
        public Entry previous() {
            if (this.nextIndex <= this.min) {
                throw new NoSuchElementException();
            }
            --this.nextIndex;
            return this.path.get(this.nextIndex);
        }

        @Override
        public int nextIndex() {
            return this.nextIndex - this.min;
        }

        @Override
        public int previousIndex() {
            return this.nextIndex - this.min - 1;
        }

        @Override
        public void remove() {
            int index = this.previousIndex();
            if (index < this.min) {
                throw new IndexOutOfBoundsException();
            }
            this.path.remove(index);
        }

        @Override
        public void set(Entry e) {
            int index = this.previousIndex();
            if (index < this.min) {
                throw new IndexOutOfBoundsException();
            }
            this.path.set(index, e);
        }

        @Override
        public void add(Entry e) {
            int index = this.nextIndex();
            this.path.add(index, e);
        }

    }

    @Override
    public ListIterator<Entry> listIterator() {
        return new PathMetadataSubListWrapperListIterator(this);
    }

    @Override
    public ListIterator<Entry> listIterator(int index) {
        this.adjustToBounds(index);
        return new PathMetadataSubListWrapperListIterator(this, index);
    }

    @Override
    public List<Entry> subList(int fromIndex, int toIndex) {
        fromIndex += this.min;
        toIndex += this.min;
        // should be this.min <= fromIndex <= toIndex <= this.max
        if (this.min > fromIndex || fromIndex > toIndex || toIndex > this.max) {
            throw new IllegalArgumentException();
        }
        return new CowWrapperPathMetadataSubList(wrapper, fromIndex, toIndex);
    }

    @Override
    public CowWrapperPathMetadataSubList deepCopy() {
        return new CowWrapperPathMetadataSubList(wrapper.deepCopy(), min, max);
    }

    @Override
    public CowWrapperPathMetadataSubList shallowCopy() {
        return new CowWrapperPathMetadataSubList(wrapper, min, max);
    }

    @Override
    public PathMetadata append(String name) {
        PathMetadata pmi = new ListPathMetadata();
        pmi.addAll(this);
        pmi.add(new Entry(name));
        return pmi;
    }

    @Override
    public PathMetadata append(int index) {
        PathMetadata pmi = new ListPathMetadata();
        pmi.addAll(this);
        pmi.add(new Entry(index));
        return pmi;
    }

    @Override
    public PathMetadata parent() {
        PathMetadata pmi = new ListPathMetadata();
        for (int i = this.min; i < this.max - 1; ++i) {
            pmi.add(this.wrapper.get(i));
        }
        return pmi;
    }

    @Override
    public boolean startsWith(PathMetadata other) {
        if (other.size() > this.size()) {
            return false;
        }
        for (int i = 0; i < other.size(); ++i) {
            if (!this.wrapper.get(i + this.min).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Node resolve(Node root) {
        for (Entry entry : this) {
            if (entry.isInteger()) {
                ListNode list = (ListNode) root;
                root = list.get(entry.asInteger());
            } else if (entry.isString()) {
                MapNode map = (MapNode) root;
                root = map.get(entry.asString());
            } else {
                throw new UnsupportedOperationException("Can't apply given path to node.");
            }
        }
        return root;
    }

    @Override
    public CowWrapperPathMetadata asWrapper(COWWrapperMetadataProvider parent, boolean owned) {
        // Why would you do this though?
        return new CowWrapperPathMetadata(null, this, owned);
    }

}