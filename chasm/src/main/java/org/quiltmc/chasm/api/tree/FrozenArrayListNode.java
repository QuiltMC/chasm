/**
 *
 */
package org.quiltmc.chasm.api.tree;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.quiltmc.chasm.internal.metadata.FrozenMetadata;
import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;

public class FrozenArrayListNode extends AbstractImmutableCollection<FrozenNode> implements FrozenListNode<FrozenNode> {
    private FrozenNode[] children;
    private FrozenMetadataProvider<FrozenMetadata> metadata;

    public FrozenArrayListNode(ListNode<? extends Node> mutableListNode) {
        children = new FrozenNode[mutableListNode.size()];
        for (int i = 0; i < children.length; ++i) {
            Node node = mutableListNode.get(i);
            children[i] = node.asImmutable();
        }
        metadata = mutableListNode.getMetadata().freeze();
    }

    private FrozenArrayListNode(FrozenNode[] children, FrozenMetadataProvider<FrozenMetadata> metadata) {
        this.children = children;
        this.metadata = metadata;
    }

    @Override
    public int size() {
        return children.length;
    }

    @Override
    public boolean isEmpty() {
        return children.length > 0;
    }

    @Override
    public boolean contains(Object o) {
        if (!(o instanceof FrozenNode)) {
            return false;
        }
        for (FrozenNode child : children) {
            if (child.equals(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public FrozenMetadataProvider getMetadata() {
        return metadata;
    }

    @Override
    public Iterator<FrozenNode> iterator() {
        return new FrozenArrayIterator();
    }

    @Override
    public Object[] toArray() {
        return children.clone();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < children.length) {
            a = (T[]) Array.newInstance(a.getClass().componentType(), children.length);
        }
        System.arraycopy(children, 0, a, 0, children.length);
        return a;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Collection<?> t = this;
        if (c.size() < t.size()) {
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
    public boolean addAll(int index, Collection<? extends FrozenNode> c) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public FrozenNode get(int index) {
        return children[index];
    }

    @Override
    public FrozenNode set(int index, FrozenNode element) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public void add(int index, FrozenNode element) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public FrozenNode remove(int index) {
        throw new UnsupportedOperationException(immutableErrorString());
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < children.length; ++i) {
            if (children[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = children.length - 1; i >= 0; --i) {
            if (children[i].equals(o)) {
                return i;
            }
        }
        return -1;
    }

    private class FrozenArrayIterator implements Iterator<FrozenNode> {
        protected int nextIndex = 0;

        @Override
        public boolean hasNext() {
            return nextIndex < children.length;
        }

        @Override
        public FrozenNode next() {
            if (nextIndex >= children.length) {
                throw new NoSuchElementException();
            }
            int index = nextIndex;
            nextIndex++;
            return children[index];
        }
    }

    private final class FrozenArrayListIterator extends FrozenArrayIterator implements ListIterator<FrozenNode> {
        protected FrozenArrayListIterator() {
            super();
        }

        protected FrozenArrayListIterator(int index) {
            super();
            nextIndex = index;
        }

        @Override
        public boolean hasPrevious() {
            return nextIndex > 0;
        }

        @Override
        public FrozenNode previous() {
            if (nextIndex <= 0) {
                throw new NoSuchElementException();
            }
            --nextIndex;
            return children[nextIndex];
        }

        @Override
        public int nextIndex() {
            return nextIndex;
        }

        @Override
        public int previousIndex() {
            return nextIndex - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(immutableErrorString());
        }

        @Override
        public void set(FrozenNode e) {
            throw new UnsupportedOperationException(immutableErrorString());
        }

        @Override
        public void add(FrozenNode e) {
            throw new UnsupportedOperationException(immutableErrorString());
        }
    }

    @Override
    public ListIterator<FrozenNode> listIterator() {
        return new FrozenArrayListIterator();
    }

    @Override
    public ListIterator<FrozenNode> listIterator(int index) {
        return new FrozenArrayListIterator(index);
    }

    @Override
    public List<FrozenNode> subList(int fromIndex, int toIndex) {
        FrozenNode[] range = Arrays.copyOfRange(children, fromIndex, toIndex);
        return new FrozenArrayListNode(range, metadata);
    }

    @Override
    public ListNode<Node> asMutable() {
        return new ArrayListNode(this);
    }
}
