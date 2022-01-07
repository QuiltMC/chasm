/**
 *
 */
package org.quiltmc.chasm.internal.tree;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;

/**
 *
 */
public class CowWrapperLazyClassNode extends CowWrapperMapNode implements LazyClassNode {
    private SoftReference<CowWrapperMapNode> fullNodeCache = null;

    private enum FullNodeSentinelKey {
        FULL_NODE
    }

    /**
     * @param <P>
     * @param <W>
     * @param parent
     * @param key
     * @param lazyClassNode
     * @param owned
     */
    public <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperLazyClassNode(
            AbstractCowWrapperNode<P, W> parent,
            Object key, LazyClassNode lazyClassNode,
            boolean owned) {
        super(parent, key, lazyClassNode, owned);
    }

    /**
     * @param cowWrapperLazyClassNode
     */
    public CowWrapperLazyClassNode(CowWrapperLazyClassNode cowWrapperLazyClassNode) {
        super(cowWrapperLazyClassNode);
    }

    @Override
    public CowWrapperLazyClassNode deepCopy() {
        CowWrapperLazyClassNode copy = new CowWrapperLazyClassNode(this);
        copy.toShared();
        copy.toOwned();
        if (!this.isOwned()) {
            copy.toShared();
        }
        copy.object = this.object.deepCopy();
        if (this.fullNodeCache != null) {
            CowWrapperMapNode wrapped = this.fullNodeCache.get();
            if (wrapped != null) {
                copy.fullNodeCache = new SoftReference<>(wrapped.deepCopy());
            }
        }
        return copy;
    }

    @Override
    public CowWrapperLazyClassNode shallowCopy() {
        return new CowWrapperLazyClassNode(this);
    }

    @Override
    public MapNode getFullNodeOrNull() {
        if (this.fullNodeCache == null) {
            return null;
        }
        return this.fullNodeCache.get();
    }

    private LazyClassNode getLazyClassNodeObject() { return (LazyClassNode) this.object; }

    @Override
    public MapNode getFullNode() {
        MapNode fullNode = this.getFullNodeOrNull();
        if (fullNode == null) {
            CowWrapperMapNode fullNodeWrapper = getLazyClassNodeObject().getFullNode().asWrapper(this,
                    FullNodeSentinelKey.FULL_NODE, isOwned());
            this.fullNodeCache = new SoftReference<>(fullNodeWrapper);
        }
        return fullNode;
    }

    @Override
    public ClassReader getClassReader() {
        return getLazyClassNodeObject().getClassReader();
    }

    private static final class CowWrapperLazyClassNodeNonLazyEntry implements Entry<String, Node> {
        private final CowWrapperLazyClassNodeNonLazyEntrySet parentSet;
        private final String key;
        private Reference<Entry<String, Node>> wrapped;

        CowWrapperLazyClassNodeNonLazyEntry(CowWrapperLazyClassNodeNonLazyEntrySet parent,
                Entry<String, Node> wrapped) {
            this.parentSet = parent;
            this.key = wrapped.getKey();
            this.wrapped = new WeakReference<>(wrapped);
        }

        @Override
        public String getKey() { return key; }

        @Override
        public Node getValue() {
            if (this.wrapped == null) {
                return null;
            }

            Entry<String, Node> entry = this.wrapped.get();
            if (entry == null) {
                return null;
            }

            return entry.getValue();
        }

        @Override
        public Node setValue(Node value) {
            return null;
        }

    }

    private static final class CowWrapperLazyClassNodeNonLazyEntrySet implements Set<Entry<String, Node>> {
        private final CowWrapperLazyClassNode self;

        private Map<Entry<String, Node>, CowWrapperLazyClassNodeNonLazyEntry> entryCache = null;

        CowWrapperLazyClassNodeNonLazyEntrySet(CowWrapperLazyClassNode self) {
            this.self = self;
        }

        private Set<Entry<String, Node>> getWrappedSet() {
            return this.self.getLazyClassNodeObject().getNonLazyEntrySet();
        }

        @Override
        public int size() {
            return this.getWrappedSet().size();
        }

        @Override
        public boolean isEmpty() { return this.getWrappedSet().isEmpty(); }

        @Override
        public boolean contains(Object o) {
            return this.getWrappedSet().contains(o);
        }

        private static final class CowWrapperLazyClassNodeNonLazyEntrySetIterator
                implements Iterator<Entry<String, Node>> {
            CowWrapperLazyClassNodeNonLazyEntrySet parent;
            Set<Entry<String, Node>> sourceSet;
            Iterator<Entry<String, Node>> wrapped;
            int skipCount = 0;

            CowWrapperLazyClassNodeNonLazyEntrySetIterator(
                    CowWrapperLazyClassNodeNonLazyEntrySet entrySet) {
                this.parent = entrySet;
                this.sourceSet = this.parent.getWrappedSet();
                this.wrapped = sourceSet.iterator();
            }

            private void ensureMatchingSource() {
                if (this.sourceSet != this.parent.getWrappedSet()) {
                    this.sourceSet = this.parent.getWrappedSet();
                    this.wrapped = sourceSet.iterator();
                    for (int i = 0; i < skipCount; ++i) {
                        wrapped.next();
                    }
                }
            }

            @Override
            public boolean hasNext() {
                this.ensureMatchingSource();
                return this.wrapped.hasNext();
            }

            @Override
            public Entry<String, Node> next() {
                this.ensureMatchingSource();
                Entry<String, Node> entryKey = this.wrapped.next();
                ++skipCount;
                return this.parent.getEntryWrapper(entryKey);
            }
        }

        @Override
        public Iterator<Entry<String, Node>> iterator() {
            return new CowWrapperLazyClassNodeNonLazyEntrySetIterator(this);
        }

        private CowWrapperLazyClassNodeNonLazyEntry getCachedEntryWrapper(Entry<String, Node> entry) {
            if (this.entryCache == null) {
                return null;
            }
            return this.entryCache.get(entry);
        }

        private CowWrapperLazyClassNodeNonLazyEntry getEntryWrapper(Entry<String, Node> wrap) {
            CowWrapperLazyClassNodeNonLazyEntry entry = this.getCachedEntryWrapper(wrap);
            if (entry == null) {
                entry = new CowWrapperLazyClassNodeNonLazyEntry(this, wrap);
                this.setEntry(wrap, entry);
            }
            return entry;
        }

        private void setEntry(Entry<String, Node> keyEntry, CowWrapperLazyClassNodeNonLazyEntry entry) {
            if (this.entryCache == null) {
                this.entryCache = new WeakHashMap<>();
            }
            this.entryCache.put(keyEntry, entry);
        }

        private void requireFullyGeneratedEntryCache() {
            // just checking the size of the entry cache clears stale entries
            if (this.entryCache.size() < this.getWrappedSet().size()) {
                for (Entry<String, Node> entry : this.getWrappedSet()) {
                    this.getEntryWrapper(entry);
                }
            }
        }

        @Override
        public Object[] toArray() {
            this.requireFullyGeneratedEntryCache();
            return this.entryCache.values().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            this.requireFullyGeneratedEntryCache();
            return this.entryCache.values().toArray(a);
        }

        @Override
        public boolean add(Entry<String, Node> e) {
            Node value = e.getValue();
            return getOwnedEntrySet().add(e);
        }

        private Set<Entry<String, Node>> getOwnedEntrySet() {
            Set<Entry<String, Node>> set = this.getWrappedSet();
            if (this.self.toOwned()) {
                Set<Entry<String, Node>> newSet = this.getWrappedSet();
                if (newSet != set) {
                    this.entryCache = null;
                    set = newSet;
                }
            }
            return set;
        }

        @Override
        public boolean remove(Object o) {
            if (this.getOwnedEntrySet().remove(o)) {
                if (this.entryCache != null) {
                    this.entryCache.remove(o);
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return this.getWrappedSet().containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends Entry<String, Node>> c) {
            return this.getOwnedEntrySet().addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            Iterator<Entry<String, Node>> iter = this.getOwnedEntrySet().iterator();
            boolean changed = false;
            for (Entry<String, Node> entry = iter.next(); iter.hasNext(); entry = iter.next()) {
                if (c.contains(entry)) {
                    iter.remove();
                    if (this.entryCache != null) {
                        this.entryCache.remove(entry);
                    }
                    changed = true;
                }
            }
            return changed;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            if (this.getOwnedEntrySet().removeAll(c)) {
                if (this.entryCache != null) {
                    this.entryCache.keySet().removeAll(c);
                }
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            this.getOwnedEntrySet().clear();
            if (this.entryCache != null) {
                this.entryCache.clear();
            }
        }

    }

    @Override
    public Set<Entry<String, Node>> getNonLazyEntrySet() {
        return new CowWrapperLazyClassNodeNonLazyEntrySet(this);
    }

    @Override
    public <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperLazyClassNode asWrapper(
            AbstractCowWrapperNode<P, W> parent, Object key, boolean owned) {
        if (this.wrapsObject(parent) && this.checkKey(key) && this.isOwned() == owned) {
            return this;
        }
        CowWrapperLazyClassNode copy = new CowWrapperLazyClassNode(parent, key, this.getLazyClassNodeObject(),
                this.isOwned());
        copy.toOwned(owned);
        return copy;
    }

}
