/**
 *
 */
package org.quiltmc.chasm.api.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import org.quiltmc.chasm.api.util.CowWrapper;
import org.quiltmc.chasm.internal.tree.AbstractCowWrapperNode;
import org.quiltmc.chasm.internal.util.AbstractChildCowWrapper;
import org.quiltmc.chasm.internal.util.ListWrapperListIterator;
import org.quiltmc.chasm.internal.util.ListWrapperSubList;
import org.quiltmc.chasm.internal.util.ReadOnlyListWrapperIterator;

/**
 *
 */
public class CowWrapperListNode extends AbstractCowWrapperNode<ArrayListNode, CowWrapperListNode>
        implements ListNode, RandomAccess {
    private List<AbstractCowWrapperNode<Node, ? extends CowWrapper>> wrapperCache;

    /**
     * @param object
     * @param owned
     * @param parent
     */
    public <P extends Node, W extends AbstractCowWrapperNode<P, W>, K extends Object> CowWrapperListNode(
            AbstractCowWrapperNode<P, W> parent, K key, ArrayListNode object, boolean owned) {
        super(parent, key, object, owned);
    }

    protected CowWrapperListNode(CowWrapperListNode cowWrapperListNode) {
        super(cowWrapperListNode);
    }

    @Override
    public CowWrapperListNode shallowCopy() {
        return new CowWrapperListNode(this);
    }

    @Override
    public <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperListNode asWrapper(
            AbstractCowWrapperNode<P, W> parent,
            Object key, boolean owned) {
        if (this.checkParentLink(parent)) {
            if (this.isOwned() == owned) {
                return this;
            } else {
                CowWrapperListNode copy = new CowWrapperListNode(this);
                copy.toOwned(owned);
                return copy;
            }
        } else {
            throw new IllegalArgumentException();
        }
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
    public Iterator<Node> iterator() {
        return new ReadOnlyListWrapperIterator<>(this.object);
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
    public boolean add(Node e) {
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
    public boolean addAll(Collection<? extends Node> c) {
        this.toOwned();
        return this.object.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Node> c) {
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
    public Node get(int index) {
        return this.object.get(index);
    }

    @Override
    public Node set(int index, Node element) {
        this.toOwned();
        return this.object.get(index);
    }

    @Override
    public void add(int index, Node element) {
        this.toOwned();
        this.object.add(index, element);
    }

    @Override
    public Node remove(int index) {
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

    @Override
    public ListIterator<Node> listIterator() {
        return new ListWrapperListIterator<>(this);
    }

    @Override
    public ListIterator<Node> listIterator(int index) {
        return new ListWrapperListIterator<>(this, index);
    }

    @Override
    public List<Node> subList(int fromIndex, int toIndex) {
        return new ListWrapperSubList<>(this, fromIndex, toIndex);
    }

    @Override
    protected CowWrapperListNode castThis() {
        return this;
    }

    @Override
    public CowWrapperListNode deepCopy() {
        CowWrapperListNode copy = new CowWrapperListNode(this);
        copy.toShared();
        copy.toOwned();
        if (copy.object == this.object) {
            copy.object = this.object.deepCopy();
        } else {
            for (int i = 0; i < this.object.size(); ++i) {
                if (copy.object.get(i) == this.object.get(i)) {
                    copy.object.set(i, this.object.get(i).deepCopy());
                }
            }
        }
        return copy;
    }

    @Override
    protected <C> void updateThisWrapper(Object key, CowWrapper childWrapper, C contents) {
        if (key == AbstractChildCowWrapper.SentinelKeys.METADATA) {
            super.updateThisWrapper(key, childWrapper, contents);
            return;
        }
        final int i = (Integer) key;
        final Node contained = this.object.get(i);
        if (!(childWrapper instanceof AbstractCowWrapperNode<?, ?>)) {
            throw new IllegalArgumentException();
        }
        AbstractCowWrapperNode<Node, ? extends CowWrapper> child = (AbstractCowWrapperNode<Node, ? extends CowWrapper>) childWrapper;

        if (this.wrapperCache == null) {
            final ArrayList<AbstractCowWrapperNode<Node, ? extends CowWrapper>> cache;
            this.wrapperCache = cache = new ArrayList<>();
            cache.ensureCapacity(i + 1);
        }
        if (this.wrapperCache.size() <= i) {
            do {
                this.wrapperCache.add(null);
            } while (this.wrapperCache.size() <= i);
        } else {
            final Node wrapper = this.wrapperCache.get(i);
            if (wrapper == child && ((CowWrapper) wrapper).wrapsObject(contents)) {
                return;
            }
        }

        this.wrapperCache.set(i, child);
        CowWrapper wrapper = this.wrapperCache == null || this.wrapperCache.size() < i ? null
                : this.wrapperCache.get(i);

    }

}
