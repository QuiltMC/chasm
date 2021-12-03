package org.quiltmc.chasm.api.tree;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;
import org.quiltmc.chasm.internal.tree.frozencollection.AbstractImmutableCollection;

public class FrozenLinkedHashMapNode implements FrozenMapNode {
    private static final String FROZEN_LINKED_HASH_MAP_NODES_ARE_IMMUTABLE = "FrozenLinkedHashMapNodes are immutable.";
    private final FrozenMetadataProvider metadata;
    private final Map<String, FrozenNode> map;
    private final String[] iterationOrder;
    private LazyFrozenEntrySet entrySet;
    private FrozenLinkedKeySet keySet;
    private FrozenLinkedValueCollection valueCollection;


    /**
     * Freeze a {@link LinkedHashMapNode} as a new {@link FrozenLinkedHashMapNode}.
     *
     * @param linkedMapNode
     * @param metadata
     */
    public FrozenLinkedHashMapNode(MapNode<Node> linkedMapNode) {
        map = new HashMap<>(linkedMapNode.size());
        for (Map.Entry<String, Node> mutableEntry : linkedMapNode.entrySet()) {
            String key = mutableEntry.getKey();
            FrozenNode frozen = mutableEntry.getValue().asImmutable();
            map.put(key, frozen);
        }

        iterationOrder = linkedMapNode.keySet().toArray(new String[linkedMapNode.size()]);
        metadata = linkedMapNode.getMetadata().freeze();
        entrySet = null;
        keySet = null;
        valueCollection = null;
    }

    @Override
    public FrozenMetadataProvider getMetadata() {
        return metadata;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException(FROZEN_LINKED_HASH_MAP_NODES_ARE_IMMUTABLE);
    }

    @Override
    public boolean containsKey(Object k) {
        if (!(k instanceof String)) {
            return false;
        }
        return map.containsKey(k);
    }

    @Override
    public boolean containsValue(Object v) {
        if (!(v instanceof FrozenNode)) {
            return false;
        }
        for (FrozenNode f : map.values()) {
            if (f.equals(v)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<Map.Entry<String, FrozenNode>> entrySet() {
        if (entrySet == null) {
            entrySet = new LazyFrozenEntrySet();
        }
        return entrySet;
    }

    @Override
    public int size() {
        return iterationOrder.length;
    }

    @Override
    public boolean isEmpty() {
        return iterationOrder.length > 0;
    }

    @Override
    public FrozenNode get(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        return map.get(key);
    }

    @Override
    public FrozenNode put(String key, FrozenNode value) {
        throw new UnsupportedOperationException(FROZEN_LINKED_HASH_MAP_NODES_ARE_IMMUTABLE);
    }

    @Override
    public FrozenNode remove(Object key) {
        throw new UnsupportedOperationException(FROZEN_LINKED_HASH_MAP_NODES_ARE_IMMUTABLE);
    }

    @Override
    public void putAll(Map<? extends String, ? extends FrozenNode> m) {
        throw new UnsupportedOperationException(FROZEN_LINKED_HASH_MAP_NODES_ARE_IMMUTABLE);
    }

    @Override
    public Set<String> keySet() {
        if (keySet == null) {
            keySet = new FrozenLinkedKeySet();
        }
        return keySet;
    }

    @Override
    public Collection<FrozenNode> values() {
        if (valueCollection == null) {
            return new FrozenLinkedValueCollection();
        }
        return valueCollection;
    }

    @Override
    public MapNode<Node> asMutable() {
        return new LinkedHashMapNode(this);
    }


    private static final class FrozenEntry implements Map.Entry<String, FrozenNode> {
        private final String key;
        private final FrozenNode value;

        FrozenEntry(String key, FrozenNode value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public FrozenNode getValue() {
            return value;
        }

        @Override
        public FrozenNode setValue(FrozenNode value) {
            throw new UnsupportedOperationException("FrozenEntries are immutable.");
        }

    }

    private final class LazyFrozenEntrySet extends AbstractImmutableCollection<Entry<String, FrozenNode>>
            implements Set<Entry<String, FrozenNode>> {
        private FrozenEntry[] frozenEntries = new FrozenEntry[iterationOrder.length];

        @Override
        public int size() {
            return FrozenLinkedHashMapNode.this.size();
        }

        @Override
        public boolean isEmpty() {
            return FrozenLinkedHashMapNode.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Entry<?, ?>)) {
                return false;
            }
            Entry<?, ?> unknownEntry = (Entry<?, ?>) o;

            Object objKey = unknownEntry.getKey();
            if (!(objKey instanceof String)) {
                return false;
            }
            String key = (String) objKey;
            FrozenNode containedValue = get(key);
            if (containedValue == null) {
                return false;
            }

            Object objValue = unknownEntry.getValue();
            return containedValue.equals(objValue);
        }

        @Override
        public Iterator<Entry<String, FrozenNode>> iterator() {
            if (frozenEntries == null) {
                frozenEntries = new FrozenEntry[iterationOrder.length];
            }
            return new Iterator<>() {
                private int nextIndex;
                {
                    nextIndex = 0;
                }

                @Override
                public boolean hasNext() {
                    return nextIndex < iterationOrder.length;
                }

                @Override
                public Entry<String, FrozenNode> next() {
                    if (nextIndex >= iterationOrder.length) {
                        throw new NoSuchElementException("Iterator beyond size of this Node.");
                    }
                    int index = nextIndex;
                    nextIndex++;
                    return LazyFrozenEntrySet.this.getFrozenEntryAt(index);
                }

            };
        }

        private FrozenEntry getFrozenEntryAt(int i) {
            FrozenEntry entry = frozenEntries[i];
            if (entry == null) {
                String key = iterationOrder[i];
                FrozenNode value = map.get(key);
                entry = new FrozenEntry(key, value);
                frozenEntries[i] = entry;
            }
            return entry;
        }

        @Override
        public Object[] toArray() {
            Object[] out = new Object[frozenEntries.length];
            for (int i = 0; i < frozenEntries.length; ++i) {
                out[i] = getFrozenEntryAt(i);
            }
            return out;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T[] toArray(T[] a) {
            if (a.length < frozenEntries.length) {
                a = (T[]) Array.newInstance(a.getClass().componentType(), frozenEntries.length);
            }
            for (int i = 0; i < frozenEntries.length; ++i) {
                a[i] = (T) getFrozenEntryAt(i);
            }
            return a;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!contains(o)) {
                    return false;
                }
            }
            return true;
        }
    }

    private final class FrozenLinkedKeySet extends AbstractImmutableCollection<String> implements Set<String> {
        @Override
        public int size() {
            return FrozenLinkedHashMapNode.this.size();
        }

        @Override
        public boolean isEmpty() {
            return FrozenLinkedHashMapNode.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return containsKey(o);
        }

        @Override
        public Iterator<String> iterator() {
            return new Iterator<>() {
                private int nextIndex = 0;
                @Override
                public boolean hasNext() {
                    return nextIndex < iterationOrder.length;
                }
                @Override
                public String next() {
                    if (nextIndex >= iterationOrder.length) {
                        throw new NoSuchElementException();
                    }
                    int index = nextIndex;
                    nextIndex++;
                    return iterationOrder[index];
                }
            };
        }

        @Override
        public Object[] toArray() {
            Object[] out = new Object[iterationOrder.length];
            System.arraycopy(iterationOrder, 0, out, 0, iterationOrder.length);
            return out;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            if (a.length < iterationOrder.length) {
                a = (T[]) Array.newInstance(a.getClass().componentType(), iterationOrder.length);
            }
            System.arraycopy(iterationOrder, 0, a, 0, iterationOrder.length);
            return a;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!containsKey(o)) {
                    return false;
                }
            }
            return true;
        }
    }

    private final class FrozenLinkedValueCollection extends AbstractImmutableCollection<FrozenNode> {
        @Override
        public int size() {
            return FrozenLinkedHashMapNode.this.size();
        }

        @Override
        public boolean isEmpty() {
            return FrozenLinkedHashMapNode.this.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return map.containsValue(o);
        }

        @Override
        public Iterator<FrozenNode> iterator() {
            return new Iterator<>() {
                private int nextIndex;

                @Override
                public boolean hasNext() {
                    return nextIndex < iterationOrder.length;
                }

                @Override
                public FrozenNode next() {
                    if (nextIndex >= iterationOrder.length) {
                        throw new NoSuchElementException();
                    }
                    int index = nextIndex;
                    nextIndex++;
                    return map.get(iterationOrder[index]);
                }
            };
        }

        @Override
        public Object[] toArray() {
            Object[] out = new Object[iterationOrder.length];
            for (int i = 0; i < out.length; ++i) {
                out[i] = map.get(iterationOrder[i]);
            }
            return out;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            if (a.length < iterationOrder.length) {
                a = (T[]) Array.newInstance(a.getClass().componentType(), iterationOrder.length);
            }
            for (int i = 0; i < iterationOrder.length; ++i) {
                a[i] = (T) map.get(iterationOrder[i]);
            }
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
    }
}
