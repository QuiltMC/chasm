/**
 *
 */
package org.quiltmc.chasm.internal.metadata;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.quiltmc.chasm.api.metadata.COWWrapperMetadataProvider;
import org.quiltmc.chasm.api.metadata.CowWrapperMetadata;
import org.quiltmc.chasm.api.metadata.Metadata;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.metadata.ListPathMetadata.Entry;

/**
 *
 */
public class CowWrapperPathMetadata extends CowWrapperMetadata<PathMetadata>
        implements PathMetadata {

    /**
     * @param parent
     * @param object
     * @param owned
     */
    public CowWrapperPathMetadata(COWWrapperMetadataProvider parent, PathMetadata object, boolean owned) {
        super(parent, ListPathMetadata.class, object, owned);
    }

    /**
     * @param other
     */
    public CowWrapperPathMetadata(CowWrapperMetadata<PathMetadata> other) {
        super(other);
    }

    @Override
    public int size() {
        return this.object.size();
    }

    @Override
    public boolean isEmpty() { return this.object.isEmpty(); }

    @Override
    public boolean contains(Object o) {
        return this.object.contains(o);
    }

    @Override
    public Iterator<Entry> iterator() {
        return new PathMetadataIterator<>(this.object);
    }

    @Override
    public Object[] toArray() {
        return this.object.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.object.toArray(a);
    }

    @Override
    public boolean add(Entry e) {
        this.toOwned();
        return this.object.add(e);
    }

    @Override
    public boolean remove(Object o) {
        this.toOwned();
        return this.object.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.object.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Entry> c) {
        this.toOwned();
        return this.object.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Entry> c) {
        this.toOwned();
        return this.object.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        this.toOwned();
        return this.object.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        this.toOwned();
        return this.object.retainAll(c);
    }

    @Override
    public void clear() {
        this.toOwned();
        this.object.clear();
    }

    @Override
    public Entry get(int index) {
        return this.object.get(index);
    }

    @Override
    public Entry set(int index, Entry element) {
        this.toOwned();
        return this.object.set(index, element);
    }

    @Override
    public void add(int index, Entry element) {
        this.toOwned();
        this.object.add(index, element);
    }

    @Override
    public Entry remove(int index) {
        this.toOwned();
        return this.object.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.object.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.object.lastIndexOf(o);
    }

    private static class PathMetadataListIterator extends PathMetadataIterator<CowWrapperPathMetadata>
            implements ListIterator<ListPathMetadata.Entry> {


        public PathMetadataListIterator(CowWrapperPathMetadata wrapper) {
            super(wrapper);
        }

        public PathMetadataListIterator(CowWrapperPathMetadata wrapper, int nextIndex) {
            super(wrapper, nextIndex);
        }

        @Override
        public boolean hasPrevious() {
            return this.nextIndex > 0;
        }

        @Override
        public Entry previous() {
            if (this.nextIndex <= 0) {
                throw new NoSuchElementException();
            }
            --this.nextIndex;
            return this.path.get(nextIndex);
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
            this.path.remove(this.previousIndex());
        }

        @Override
        public void set(Entry e) {
            this.path.set(this.previousIndex(), e);
        }

        @Override
        public void add(Entry e) {
            this.path.add(this.nextIndex, e);
        }

    }
    @Override
    public ListIterator<Entry> listIterator() {
        return new PathMetadataListIterator(this);
    }

    @Override
    public ListIterator<Entry> listIterator(int index) {
        return new PathMetadataListIterator(this, index);
    }

    @Override
    public List<Entry> subList(int fromIndex, int toIndex) {
        return new CowWrapperPathMetadataSubList(this, fromIndex, toIndex);
    }

    @Override
    public CowWrapperPathMetadata deepCopy() {
        return new CowWrapperPathMetadata(this);
    }

    @Override
    public CowWrapperPathMetadata shallowCopy() {
        return new CowWrapperPathMetadata(this);
    }

    @Override
    public CowWrapperPathMetadata append(String name) {
        CowWrapperPathMetadata copy = new CowWrapperPathMetadata(this);
        copy.add(new Entry(name));
        return copy;
    }

    @Override
    public CowWrapperPathMetadata append(int index) {
        CowWrapperPathMetadata copy = new CowWrapperPathMetadata(this);
        copy.add(new Entry(index));
        return copy;
    }

    @Override
    public CowWrapperPathMetadata parent() {
        CowWrapperPathMetadata copy = new CowWrapperPathMetadata(this);
        copy.remove(copy.size());
        return copy;
    }

    @Override
    public boolean startsWith(PathMetadata other) {
        if (other.size() > this.size()) {
            return false;
        }
        for (int i = 0; i < other.size(); ++i) {
            if (!this.get(i).equals(other.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Node resolve(Node root) {
        return this.object.resolve(root);
    }

    @Override
    protected <C> void updateThisWrapper(Object objKey, CowWrapper child, C objContents) {
        int key = (Integer) objKey;
        Entry current = this.get(key);
        Entry contents = (Entry) objContents;
        if (contents != current || child.isOwned() != this.isOwned()) {
            this.set(key, contents);
            this.toOwned(child.isOwned());
        }
    }

    @Override
    public <T extends Metadata> T asWrapper(COWWrapperMetadataProvider parent, Class<T> key, boolean owned) {
        if (key != PathMetadata.class) {
            throw new IllegalArgumentException("Illegal agument" + key);
        }
        return key.cast(new CowWrapperPathMetadata(parent, this.object, owned));
    }

}
