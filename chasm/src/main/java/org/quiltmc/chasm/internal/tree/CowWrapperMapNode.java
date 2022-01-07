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
    private CowWrapperMapNodeKeySet keySet = null;
    private CowWrapperMapNodeValueCollection valueCollection = null;
    private CowWrapperMapNodeEntrySet entrySet = null;
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
     * Constructs a shallow copy of the passed map node wrapper.
     *
     * @param cowWrapperMapNode The map node wrapper to copy.
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
        if (this.checkParentLink(parent) && this.checkKey(key) && this.isOwned() == owned) {
            return this;
        }
        CowWrapperMapNode copy = new CowWrapperMapNode(parent, key, object, this.isOwned());
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

    private void requireFullyGeneratedCache() {
        if (this.wrapperCache == null || this.wrapperCache.size() < this.object.size()) {
            for (String str : this.object.keySet()) {
                if (this.get(str) == null) {
                    throw new AssertionError(str);
                }
            }
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
        private ReadOnlyIteratorWrapper<String> iterator = null;

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
            if (this.iterator == null) {
                this.iterator = new ReadOnlyIteratorWrapper<>(this.self.object.keySet().iterator());
            }
            return this.iterator;
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
        if (this.keySet == null) {
            this.keySet = new CowWrapperMapNodeKeySet(this);
        }
        return this.keySet;
    }

    private static final class CowWrapperMapNodeValueCollection implements Collection<Node> {
        private CowWrapperMapNode self;
        private CowWrapperMapNodeValueCollectionIterator iterator = null;

        public CowWrapperMapNodeValueCollection(CowWrapperMapNode self) {
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
            if (this.iterator == null) {
                this.iterator = new CowWrapperMapNodeValueCollectionIterator(this.self);
            }
            return this.iterator;
        }


        @Override
        public Object[] toArray() {
            this.self.requireFullyGeneratedCache();
            return this.self.wrapperCache.values().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            this.self.requireFullyGeneratedCache();
            return this.self.wrapperCache.values().toArray(a);
        }

        @Override
        public boolean add(Node e) {
            // What would the key even be?
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
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return this.self.object.values().containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends Node> c) {
            // what would the DIFFERENT keys be?
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
        if (this.valueCollection == null) {
            this.valueCollection = new CowWrapperMapNodeValueCollection(this);
        }
        return this.valueCollection;
    }

    private static final class CowWrapperMapNodeEntry implements Entry<String, Node> {
        private final CowWrapperMapNode parent;
        private final String key;

        CowWrapperMapNodeEntry(CowWrapperMapNode parent, String key) {
            this.parent = parent;
            this.key = key;
        }

        @Override
        public String getKey() { return key; }

        @Override
        public Node getValue() { return this.parent.get(key); }

        @Override
        public Node setValue(Node value) {
            this.parent.toOwned();
            return this.parent.put(key, value);
        }

    }

    /**
     *
     */
    private static final class CowWrapperMapNodeEntrySet implements Set<Entry<String, Node>> {
        private final CowWrapperMapNode self;
        private Map<String, CowWrapperMapNodeEntry> entryCache = null;

        CowWrapperMapNodeEntrySet(CowWrapperMapNode self) {
            this.self = self;
        }

        private CowWrapperMapNodeEntry getEntry(String key) {
            if (this.entryCache == null) {
                this.entryCache = new HashMap<>();
            }
            CowWrapperMapNodeEntry cached = this.entryCache.get(key);
            if (cached == null) {
                cached = new CowWrapperMapNodeEntry(this.self, key);
                this.entryCache.put(key, cached);
            }
            return cached;
        }

        private void ensureFullyGeneratedCache() {
            if (this.entryCache == null || this.entryCache.size() < this.size()) {
                for (String key : this.self.object.keySet()) {
                    this.getEntry(key);
                }
            }
        }

        @Override
        public int size() {
            return this.self.size();
        }

        @Override
        public boolean isEmpty() { return this.self.isEmpty(); }

        private String getEntryKeyString(Object o) {
            if (!(o instanceof Entry<?, ?>)) {
                return null;
            }
            Entry<?, ?> vagueEntry = (Entry<?, ?>) o;
            Object objKey = vagueEntry.getKey();
            if (!(objKey instanceof String)) {
                return null;
            }
            return (String) objKey;
        }

        private Node getEntryValueNode(Object o) {
            if (!(o instanceof Entry<?, ?>)) {
                return null;
            }
            Entry<?, ?> vagueEntry = (Entry<?, ?>) o;
            Object objValue = vagueEntry.getValue();
            if (!(objValue instanceof Node)) {
                return null;
            }
            return (Node) objValue;
        }

        @Override
        public boolean contains(Object o) {
            String key = getEntryKeyString(o);
            if (key == null) {
                return false;
            }
            Node value = getEntryValueNode(o);
            Node realValue = this.self.object.get(key);
            return realValue != null && realValue.equals(value);
        }

        private static class CowWrapperMapNodeEntrySetIterator implements Iterator<Entry<String, Node>> {
            private final CowWrapperMapNodeEntrySet entrySet;
            private final CowWrapperMapNode map;

            private final Iterator<String> keyIterator;

            CowWrapperMapNodeEntrySetIterator(CowWrapperMapNodeEntrySet entrySet) {
                this.entrySet = entrySet;
                this.map = entrySet.self;
                this.keyIterator = this.map.object.keySet().iterator();
            }

            @Override
            public boolean hasNext() {
                return this.keyIterator.hasNext();
            }

            @Override
            public Entry<String, Node> next() {
                return this.entrySet.getEntry(this.keyIterator.next());
            }
        }

        @Override
        public Iterator<Entry<String, Node>> iterator() {
            return new CowWrapperMapNodeEntrySetIterator(this);
        }

        @Override
        public Object[] toArray() {
            this.ensureFullyGeneratedCache();
            return this.entryCache.values().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            this.ensureFullyGeneratedCache();
            return this.entryCache.values().toArray(a);
        }

        @Override
        public boolean add(Entry<String, Node> e) {
            String key = e.getKey();
            Node value = e.getValue();
            Node currentValue = this.self.object.get(key);
            if (currentValue != null && currentValue.equals(value)) {
                return false;
            }
            this.self.put(e.getKey(), e.getValue());
            return true;
        }

        @Override
        public boolean remove(Object o) {
            String key = getEntryKeyString(o);
            if (key == null) {
                return false;
            }
            Node value = getEntryValueNode(o);
            if (value == null) {
                return false;
            }
            return this.self.remove(key, value);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!(this.contains(o))) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends Entry<String, Node>> c) {
            boolean changed = false;
            for (Entry<String, Node> entry : c) {
                changed |= this.add(entry);
            }
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean changed = false;
            for (String key : this.self.object.keySet()) {
                Entry<String, Node> entry = this.getEntry(key);
                if (!c.contains(entry)) {
                    this.entryCache.remove(key);
                    this.self.remove(key);
                    changed = true;
                }
            }
            return changed;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean changed = false;
            for (String key : this.self.object.keySet()) {
                Entry<String, Node> entry = this.getEntry(key);
                if (c.contains(entry)) {
                    this.self.remove(key);
                    this.entryCache.remove(key);
                    changed = true;
                }
            }
            return changed;
        }

        @Override
        public void clear() {
            this.self.clear();
            this.entryCache.clear();
        }

    }

    @Override
    public Set<Entry<String, Node>> entrySet() {
        if (this.entrySet == null) {
            this.entrySet = new CowWrapperMapNodeEntrySet(this);
        }
        return this.entrySet;
    }

    @Override
    protected CowWrapperMapNode castThis() {
        return this;
    }

    @Override
    public CowWrapperMapNode deepCopy() {
        CowWrapperMapNode copy = new CowWrapperMapNode(this);
        copy.wrapperCache = null;
        copy.toShared();
        copy.toOwned(this.isOwned());
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
