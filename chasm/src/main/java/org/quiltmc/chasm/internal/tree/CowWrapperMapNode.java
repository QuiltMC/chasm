/**
 *
 */
package org.quiltmc.chasm.internal.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.quiltmc.chasm.api.tree.CowWrapperNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.collection.ReadOnlyIteratorWrapper;
import org.quiltmc.chasm.internal.cow.UpdatableCowWrapper;

/**
 *
 */
public class CowWrapperMapNode extends AbstractCowWrapperNode<MapNode, CowWrapperMapNode> implements MapNode {
    private Map<String, CowWrapperNode> wrapperCache;
    /**
     * @param parent
     * @param key
     * @param object
     * @param owned
     */
    public <P extends Node, W extends AbstractCowWrapperNode<P, W>, K> CowWrapperMapNode(
            AbstractCowWrapperNode<P, W> parent, K key,
            MapNode object, boolean owned) {
        super(parent, key, object, owned);
        this.wrapperCache = null;
    }

    /**
     * @param cowWrapperMapNode
     */
    public CowWrapperMapNode(CowWrapperMapNode cowWrapperMapNode) {
        super(cowWrapperMapNode);
    }

    @Override
    public CowWrapperMapNode shallowCopy() {
        CowWrapperMapNode copy = new CowWrapperMapNode(this);
        if (this.isOwned()) {
            copy.toShared();
            copy.toOwned();
        }
        return copy;
    }

    @Override
    public <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperMapNode asWrapper(
            AbstractCowWrapperNode<P, W> parent,
            Object key,
            boolean owned) {
        CowWrapperMapNode copy = new CowWrapperMapNode(parent, key, object, owned);
        copy.toOwned(this.isOwned());
        copy.toOwned(owned);
        return copy;
    }

    @Override
    public int size() {
        return this.object.size();
    }

    @Override
    public boolean isEmpty() { return this.object.isEmpty(); }

    @Override
    public boolean containsKey(Object key) {
        return this.object.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.object.containsValue(value);
    }

    @Override
    public Node get(Object key) {
        if (this.wrapperCache == null) {
            this.wrapperCache = new HashMap<>();
        }
        Node cached = this.wrapperCache.get(key);
        if (cached == null) {
            Node child = this.object.get(key);
            if (child == null) {
                return null;
            }
            cached = child.asWrapper(this, key, this.isOwned());
        }
        return cached;
    }

    @Override
    public Node put(String key, Node value) {
        this.toOwned();
        final Node cached = takeWrapper(key);
        this.object.put(key, value);
        return cached;
    }

    private Node takeWrapper(String key) {
        if (this.wrapperCache != null) {
            final Node cached = this.get(key);
            removeWrapper(key, cached);
            return cached;
        } else {
            return null;
        }
    }

    private Node removeCachedWrapper(String key) {
        if (this.wrapperCache != null) {
            final Node cached = this.wrapperCache.get(key);
            removeWrapper(key, cached);
            return cached;
        } else {
            return null;
        }
    }

    private void removeWrapper(String key, final Node cached) {
        if (cached != null) {
            if (cached instanceof UpdatableCowWrapper) {
                ((UpdatableCowWrapper) cached).unlinkParentWrapper();
            }
            this.wrapperCache.remove(key);
        }
    }

    @Override
    public Node remove(Object key) {
        this.toOwned();
        if (key instanceof String) {
            final Node cached = takeWrapper((String) key);
            this.object.remove(key);
            return cached;
        } else {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends Node> m) {
        this.toOwned();
        for (Entry<? extends String, ? extends Node> e : m.entrySet()) {
            String key = e.getKey();
            this.removeCachedWrapper(key);
            this.object.put(key, e.getValue());
        }
    }

    @Override
    public void clear() {
        this.toOwned();
        if (this.wrapperCache != null) {
            for (String key : this.wrapperCache.keySet()) {
                this.removeCachedWrapper(key);
                this.wrapperCache.remove(key);
            }
        }
        this.object.clear();
    }

    private static final class CowWrapperMapNodeKeySet implements Set<String> {
        private final CowWrapperMapNode self;

        public CowWrapperMapNodeKeySet(CowWrapperMapNode self) {
            this.self = self;
        }

        @Override
        public int size() {
            return this.self.object.size();
        }

        @Override
        public boolean isEmpty() { return this.self.object.isEmpty(); }

        @Override
        public boolean contains(Object o) {
            return this.self.object.containsKey(o);
        }

        @Override
        public Iterator<String> iterator() {
            return new ReadOnlyIteratorWrapper<>(this.self.object.keySet().iterator());
        }

        @Override
        public Object[] toArray() {
            return this.self.object.keySet().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return this.self.object.keySet().toArray(a);
        }

        @Override
        public boolean add(String e) {
            this.self.toOwned();
            this.self.removeCachedWrapper(e);
            return this.self.object.keySet().add(e);
        }

        @Override
        public boolean remove(Object o) {
            this.self.toOwned();
            if (o instanceof String) {
                this.self.removeCachedWrapper((String) o);
                if (this.self.object.containsKey(o)) {
                    this.self.object.remove(o);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return this.self.object.keySet().containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends String> c) {
            this.self.toOwned();
            return this.self.object.keySet().addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            this.self.toOwned();
            boolean modified = false;
            Iterator<String> keyIter = this.self.object.keySet().iterator();
            for (String key = keyIter.next(); keyIter.hasNext(); key = keyIter.next()) {
                if (!c.contains(key)) {
                    this.self.removeCachedWrapper(key);
                    this.self.wrapperCache.remove(key);
                    keyIter.remove();
                    modified = true;
                }
            }
            return modified;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            this.self.toOwned();
            boolean modified = false;
            Iterator<String> keyIter = this.self.object.keySet().iterator();
            for (String key = keyIter.next(); keyIter.hasNext(); key = keyIter.next()) {
                if (c.contains(key)) {
                    this.self.removeCachedWrapper(key);
                    this.self.wrapperCache.remove(key);
                    keyIter.remove();
                    modified = true;
                }
            }
            return modified;
        }

        @Override
        public void clear() {
            this.self.clear();
        }

    }

    @Override
    public Set<String> keySet() {
        return new CowWrapperMapNodeKeySet(this);
    }

    private static final class CowWrapperMapNodeValueCollection implements Collection<Node> {
        private CowWrapperMapNode self;

        public CowWrapperMapNodeValueCollection(CowWrapperMapNode self) {
            this.self = self;
        }

        @Override
        public int size() {
            return this.self.object.size();
        }

        @Override
        public boolean isEmpty() { return this.self.object.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return this.self.object.containsValue(o);
        }

        private static class CowWrapperMapNodeValueCollectionIterator implements Iterator<Node> {
            private CowWrapperMapNode self;
            private Iterator<String> keys;

            public CowWrapperMapNodeValueCollectionIterator(CowWrapperMapNode self) {
                this.self = self;
                this.keys = self.object.keySet().iterator();
            }

            @Override
            public boolean hasNext() {
                return this.keys.hasNext();
            }

            @Override
            public Node next() {
                return this.self.get(this.keys.next());
            }

        }

        @Override
        public Iterator<Node> iterator() {
            return new CowWrapperMapNodeValueCollectionIterator(this.self);
        }

        private void requireFullyGeneratedCache() {
            if (this.self.wrapperCache == null || this.self.wrapperCache.size() < this.self.object.size()) {
                for (String str : this.self.object.keySet()) {
                    if (this.self.get(str) == null) {
                        throw new RuntimeException();
                    }
                }
            }
        }

        @Override
        public Object[] toArray() {
            this.requireFullyGeneratedCache();
            return this.self.wrapperCache.values().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            this.requireFullyGeneratedCache();
            return this.self.wrapperCache.values().toArray(a);
        }

        @Override
        public boolean add(Node e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            for (String key : this.self.object.keySet()) {
                if (this.self.object.get(key).equals(o)) {
                    this.self.remove(key);
                    return true;
                }
            }
//            // This loop will allocate wrappers for every value, in case its equals is different
//            for (String key : this.self.object.keySet()) {
//                if (this.self.get(key).equals(o)) {
//                    this.self.remove(key);
//                    return true;
//                }
//            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return this.self.object.values().containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends Node> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            this.self.toOwned();
            boolean modified = false;
            Iterator<String> keys = this.self.object.keySet().iterator();
            for (String key = keys.next(); keys.hasNext(); key = keys.next()) {
                if (c.contains(this.self.get(key))) {
                    keys.remove();
                    this.self.removeCachedWrapper(key);
                    this.self.wrapperCache.remove(key);
                    modified = true;
                }
            }
            return modified;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            this.self.toOwned();
            boolean modified = false;
            Iterator<String> keys = this.self.object.keySet().iterator();
            for (String key = keys.next(); keys.hasNext(); key = keys.next()) {
                if (!c.contains(this.self.get(key))) {
                    keys.remove();
                    this.self.removeCachedWrapper(key);
                    this.self.wrapperCache.remove(key);
                    modified = true;
                }
            }
            return modified;
        }

        @Override
        public void clear() {
            this.self.clear();
        }
    }

    @Override
    public Collection<Node> values() {
        return new CowWrapperMapNodeValueCollection(this);
    }

    @Override
    public Set<Entry<String, Node>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected CowWrapperMapNode castThis() {
        return this;
    }

    @Override
    public CowWrapperMapNode deepCopy() {
        CowWrapperMapNode copy = new CowWrapperMapNode(this);
        copy.wrapperCache = null;
        copy.object = this.object.deepCopy();
        return copy;
    }

    @Override
    protected CowWrapperNode getCachedCowWrapperNode(Object key) {
        if (this.wrapperCache == null) {
            return null;
        }
        return this.wrapperCache.get(key);
    }

    @Override
    protected CowWrapperNode setCachedCowWrapperNode(Object key, CowWrapperNode wrapper) {
        if (this.wrapperCache == null) {
            this.wrapperCache = new HashMap<>();
        }
        return this.wrapperCache.put((String) key, wrapper);
    }

    @Override
    protected boolean clearCachedCowWrapperNodes() {
        if (this.wrapperCache == null || this.wrapperCache.isEmpty()) {
            return false;
        }
        this.wrapperCache.clear();
        return true;
    }

    @Override
    protected Node getChildNode(Object key) {
        return this.object.get(key);
    }

    @Override
    protected Node setChildNode(Object key, Node value) {
        return this.object.put((String) key, value);
    }

}
