/**
 *
 */
package org.quiltmc.chasm.api.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.quiltmc.chasm.internal.collection.MixinRandomAccessListImpl;
import org.quiltmc.chasm.internal.cow.AbstractCowWrapper;
import org.quiltmc.chasm.internal.cow.UpdatableCowWrapper;
import org.quiltmc.chasm.internal.tree.AbstractCowWrapperNode;

/**
 *
 */
public class CowWrapperListNode extends AbstractCowWrapperNode<ArrayListNode, CowWrapperListNode>
        implements ListNode, MixinRandomAccessListImpl<Node> {
    private List<CowWrapperNode> listCache = null;
    private int hashCode = System.identityHashCode(this);

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

    private CowWrapperNode setCache(int i, CowWrapperNode toCache) {
        if (this.listCache == null) {
            this.listCache = new ArrayList<>();
        }
        if (this.listCache.size() <= i) {
            expandCache(i + 1);
        }
        return this.listCache.set(i, toCache);
    }

    private void expandCache(int size) {
        if (this.listCache instanceof ArrayList<?>) {
            ((ArrayList<CowWrapperNode>) this.listCache).ensureCapacity(size);
        }
        for (int j = size - this.listCache.size(); j > 0; --j) {
            this.listCache.add(null);
        }
    }

    private CowWrapperNode getCachedWrapper(int i) {
        if (this.listCache == null || this.listCache.size() <= i) {
            return null;
        }
        return this.listCache.get(i);
    }

    private Node destroyCachedWrapper(int i) {
        if (this.listCache == null || this.listCache.size() <= i) {
            return null;
        }
        Node cached;
        if (this.listCache.size() == i + 1) {
            cached = this.listCache.remove(i);
        } else {
            cached = this.listCache.set(i, null);
        }
        if (cached instanceof UpdatableCowWrapper) {
            ((UpdatableCowWrapper) cached).unlinkParentWrapper();
        }
        return cached;
    }

    private Node removeCachedWrapper(int i) {
        if (this.listCache == null || this.listCache.size() <= i) {
            return null;
        }
        Node cached = this.listCache.remove(i);
        if (cached instanceof UpdatableCowWrapper) {
            ((UpdatableCowWrapper) cached).unlinkParentWrapper();
        }
        return cached;
    }

    private boolean clearCachedWrappers() {
        if (this.listCache == null || this.listCache.isEmpty()) {
            return false;
        }
        for (int i = this.listCache.size() - 1; i >= 0; --i) {
            AbstractCowWrapper.clearCowWrapperParent(this.listCache.get(i));
        }
        this.listCache.clear();
        return true;
    }

    @Override
    public CowWrapperListNode shallowCopy() {
        return new CowWrapperListNode(this);
    }

    @Override
    public <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperListNode asWrapper(
            AbstractCowWrapperNode<P, W> parent,
            Object key, boolean owned) {
        if (this.checkParentLink(parent) && this.checkKey(key)) {
            if (this.isOwned() == owned) {
                return this;
            } else {
                CowWrapperListNode copy = new CowWrapperListNode(this);
                copy.toOwned(owned);
                return copy;
            }
        } else {
            CowWrapperListNode copy = new CowWrapperListNode(parent, key, this.object, this.isOwned());
            copy.toOwned(owned);
            return copy;
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
    public boolean add(Node e) {
        this.toOwned();
        /// @see List#hashCode()
        if (hashCode != 0) {
            this.hashCode *= 31;
            if (e != null) {
                this.hashCode += e.hashCode();
            }
            if (this.hashCode == System.identityHashCode(this)) {
                --this.hashCode;
            }
        }
        return this.object.add(e);
    }

    @Override
    public boolean remove(Object o) {
        this.toOwned();
        int i = this.object.indexOf(o);
        if (i < 0) {
            return false;
        }
        this.removeCachedWrapper(i);
        this.object.remove(i);
        this.hashCode = 0;
        return true;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.object.containsAll(c);
    }

    @Override
    public void clear() {
        this.toOwned();
        this.clearCachedWrappers();
        this.object.clear();
        this.hashCode = 1;
    }

    public Node get(Integer i) {
        CowWrapperNode node = this.getCachedWrapper(i);
        if (node == null) {
            node = this.object.get(i).asWrapper(this, i, this.isOwned());
            this.setCache(i, node);
        }
        return node;
    }

    @Override
    public Node get(int i) {
        CowWrapperNode node = this.getCachedWrapper(i);
        if (node == null) {
            node = this.object.get(i).asWrapper(this, i, this.isOwned());
            this.setCache(i, node);
        }
        return node;
    }

    @Override
    public Node set(int index, Node element) {
        this.toOwned();
        Node old = this.get(index);
        this.destroyCachedWrapper(index);
        this.object.set(index, element);
        this.hashCode = System.identityHashCode(this);
        return old;
    }

    private void insertCacheSpace(int i) {
        if (this.listCache == null || this.listCache.size() < i) {
            return;
        }
        this.listCache.add(i, null);
    }

    @Override
    public void add(int index, Node element) {
        this.toOwned();
        this.insertCacheSpace(index);
        this.object.add(index, element);
        this.hashCode = System.identityHashCode(this);
    }

    @Override
    public Node remove(int index) {
        this.toOwned();
        Node wrapper = this.get(index);
        this.destroyCachedWrapper(index);
        this.object.remove(index);
        this.hashCode = System.identityHashCode(this);
        return wrapper;
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
            elementwiseDeepCopy(copy);
        }
        // in case the hash code is like System#identityHashCode or the default Object#hashCode
        copy.hashCode = System.identityHashCode(this);
        return copy;
    }

    private void elementwiseDeepCopy(CowWrapperListNode copy) {
        for (int i = 0; i < this.object.size(); ++i) {
            if (copy.object.get(i) == this.object.get(i)) {
                copy.set(i, this.object.get(i).deepCopy());
            }
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof List<?> && this.listEqualsHelper((List<?>) other);
    }

    @Override
    public int hashCode() {
        if (this.hashCode == System.identityHashCode(this)) {
            int code = this.listHashcodeHelper();
            if (code == System.identityHashCode(this)) {
                --code;
            }
            this.hashCode = code;
        }
        return this.hashCode;
    }

    @Override
    protected CowWrapperNode getCachedCowWrapperNode(Object key) {
        if (this.listCache == null) {
            return null;
        }
        return getCachedWrapper((Integer) key);
    }

    @Override
    protected CowWrapperNode setCachedCowWrapperNode(Object key, CowWrapperNode wrapper) {
        return this.setCache((Integer) key, wrapper);
    }

    @Override
    protected boolean clearCachedCowWrapperNodes() {
        return this.clearCachedWrappers();
    }

    @Override
    protected Node getChildNode(Object key) {
        return this.object.get((Integer) key);
    }

    @Override
    protected Node setChildNode(Object key, Node value) {
        return this.object.set((Integer) key, value);
    }

}
