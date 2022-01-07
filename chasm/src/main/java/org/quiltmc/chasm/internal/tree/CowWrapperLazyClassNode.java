/**
 *
 */
package org.quiltmc.chasm.internal.tree;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.tree.CowWrapperNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.collection.ReadOnlyIteratorWrapper;

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
     * @param lazyClassMapNode
     * @param owned
     */
    public <K, P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperLazyClassNode(
            AbstractCowWrapperNode<P, W> parent,
            K key, LazyClassMapNode lazyClassMapNode,
            boolean owned) {
        super(parent, key, lazyClassMapNode, owned);
    }

    /**
     * @param cowWrapperLazyClassNode
     */
    public CowWrapperLazyClassNode(CowWrapperLazyClassNode cowWrapperLazyClassNode) {
        super(cowWrapperLazyClassNode);
    }

    @Override
    public CowWrapperLazyClassNode deepCopy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CowWrapperLazyClassNode shallowCopy() {
        // TODO Auto-generated method stub
        return null;
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
        private SoftReference<Entry<String, Node>> wrapped;

        CowWrapperLazyClassNodeNonLazyEntry(CowWrapperLazyClassNodeNonLazyEntrySet parent, Entry<String, Node> wrapped,
                ReferenceQueue<Entry<String, Node>> queue) {
            this.parentSet = parent;
            this.key = wrapped.getKey();
            this.wrapped = new SoftReference<>(wrapped, queue);
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

            Node value = entry.getValue();
            return value;
        }

        @Override
        public Node setValue(Node value) {
            return null;
        }

    }

    private static final class CowWrapperLazyClassNodeNonLazyEntrySet implements Set<Entry<String, Node>> {
        private final CowWrapperLazyClassNode self;
        private final Set<Entry<String, Node>> wrappedSet;
        private final Map<String, CowWrapperNode> wrapperCache = null;

        private Map<String, CowWrapperLazyClassNodeNonLazyEntry> entryCache = null;
        private ReferenceQueue<Entry<String, Node>> deadEntryQueue = null;

        CowWrapperLazyClassNodeNonLazyEntrySet(CowWrapperLazyClassNode self, Set<Entry<String, Node>> set) {
            this.self = self;
            this.wrappedSet = set;
        }

        @Override
        public int size() {
            return this.wrappedSet.size();
        }

        @Override
        public boolean isEmpty() { return this.wrappedSet.isEmpty(); }

        @Override
        public boolean contains(Object o) {
            return this.wrappedSet.contains(o);
        }

        @Override
        public Iterator<Entry<String, Node>> iterator() {
            return new ReadOnlyIteratorWrapper<>(this.wrappedSet.iterator());
        }

        private CowWrapperLazyClassNodeNonLazyEntry getCachedEntry(String key) {
            if (this.entryCache == null) {
                return null;
            }
            return this.entryCache.get(key);
        }

        private CowWrapperLazyClassNodeNonLazyEntry getEntry(Entry<String, Node> wrap) {
            String key = wrap.getKey();
            CowWrapperLazyClassNodeNonLazyEntry entry = this.getCachedEntry(key);
            if (entry == null) {
                entry = new CowWrapperLazyClassNodeNonLazyEntry(this, wrap, this.deadEntryQueue);
                this.setEntry(entry);
            }
            return entry;
        }

        private void setEntry(CowWrapperLazyClassNodeNonLazyEntry entry) {
            if (this.entryCache == null) {
                this.entryCache = new HashMap<>();
            }
            this.entryCache.put(entry.getKey(), entry);
        }

        private void removeCachedEntry(String key, Node value) {
            if (this.entryCache != null) {
                CowWrapperLazyClassNodeNonLazyEntry entry = this.entryCache.remove(key);
                Node containedValue = entry.getValue();
                if (containedValue != value && containedValue != null && !containedValue.equals(value)) {
                    this.entryCache.put(key, entry);
                }
            }
            if (this.wrapperCache != null) {
                this.wrapperCache.remove(key, value);
            }
        }

        private void requireFullyGeneratedEntryCache() {
            if (this.entryCache.size() < this.wrappedSet.size()) {
                for (Entry<String, Node> entry : this.wrappedSet) {
                    this.getEntry(entry);
                }
            }
            if (this.entryCache.size() > this.wrappedSet.size()) {
                Reference<? extends Entry<String, Node>> deadEntry = deadEntryQueue.poll();
                while (deadEntry != null) {
                    if (deadEntry.refersTo(null)) {
                        throw new AssertionError();
                    }
                    this.removeCachedEntry(deadEntry.get().getKey(), deadEntry.get().getValue());
                    deadEntry = deadEntryQueue.poll();
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
            if (!this.self.toOwned()) {
                this.removeCachedEntry(e.getKey(), value);
            }
            return this.wrappedSet.add(e);
        }

        @Override
        public boolean remove(Object o) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends Entry<String, Node>> c) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void clear() {
            // TODO Auto-generated method stub

        }

    }

    @Override
    public Set<Entry<String, Node>> getNonLazyEntrySet() {

    }

    @Override
    public <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperLazyClassNode asWrapper(
            AbstractCowWrapperNode<P, W> parent, Object key, boolean owned) {
        // TODO Auto-generated method stub
        return null;
    }

}
