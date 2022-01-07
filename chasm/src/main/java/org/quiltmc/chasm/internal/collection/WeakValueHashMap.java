/**
 *
 */
package org.quiltmc.chasm.internal.collection;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.quiltmc.chasm.internal.collection.WeakValueHashMap.WeakValueHashMapEntrySet.WeakValueHashMapEntry;

public class WeakValueHashMap<K, V> implements Map<K, V> {

    protected final Map<K, WeakReference<V>> map;
    private final ReferenceQueue<V> deadValues;
    private final Map<Reference<? extends V>, K> inverseMap;
    protected WeakValueHashMapValueCollection<K, V> valueCollection = null;
    protected WeakValueHashMapEntrySet<K, V> entrySet = null;

    /**
     * Constructs a new {@link WeakValueHashMap} with default initial capacity.
     */
    public WeakValueHashMap() {
        this.map = new HashMap<>();
        this.inverseMap = new IdentityHashMap<>();
        this.deadValues = new ReferenceQueue<>();
    }

    /**
     * Constructs a new {@link WeakValueHashMap} with the passed
     *
     * @param initialCapacity
     */
    public WeakValueHashMap(int initialCapacity) {
        this.map = new HashMap<>(initialCapacity);
        this.inverseMap = new IdentityHashMap<>(initialCapacity);
        this.deadValues = new ReferenceQueue<>();
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        this.removeDeadValues();
        WeakReference<V> ref = this.map.get(key);
        if (ref == null) {
            this.removeInner(key);
            return null;
        }
        return ref.get();
    }

    private void removeDeadValues() {
        do {
            Reference<? extends V> ref = this.deadValues.poll();
            if (ref == null) {
                return;
            }
            K key = this.inverseMap.get(ref);
            WeakReference<V> value = this.map.get(key);
            if (value != ref || key == null) {
                continue;
            }
            this.map.remove(key, ref);
            this.inverseMap.remove(ref, key);
            if (this.entrySet != null && this.entrySet.entryCache != null) {
                this.entrySet.entryCache.remove(ref);
            }
        } while (true);
    }

    @Override
    public V put(K key, V value) {
        this.removeDeadValues();
        return this.innerPut(key, value);
    }

    protected final V innerPut(K key, V value) {
        WeakReference<V> ref = new WeakReference<>(value, this.deadValues);
        WeakReference<V> oldRef = this.map.put(key, ref);
        this.inverseMap.remove(oldRef);
        this.inverseMap.put(ref, key);
        if (this.entrySet != null) {
            Map<K, WeakValueHashMapEntrySet.WeakValueHashMapEntry<K, V>> cache = this.entrySet.entryCache;
            if (cache != null) {
                WeakValueHashMapEntry<K, V> entry = cache.get(key);
                entry.value = ref;
            }
        }
        if (oldRef == null) {
            return null;
        }
        return oldRef.get();
    }

    @Override
    public V remove(Object key) {
        this.removeDeadValues();
        return removeInner(key);
    }

    private V removeInner(Object key) {
        WeakReference<V> ref = this.map.remove(key);
        this.inverseMap.remove(ref);
        if (ref == null) {
            return null;
        }
        return ref.get();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.removeDeadValues();
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            this.innerPut(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.map.clear();
        this.inverseMap.clear();
        while (this.deadValues.poll() != null) {}
    }

    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }

    protected static class WeakValueHashMapValueCollection<K, V> implements Collection<V> {
        private final WeakValueHashMap<K, V> parent;

        WeakValueHashMapValueCollection(WeakValueHashMap<K, V> parent) {
            this.parent = parent;
        }

        @Override
        public int size() {
            return this.parent.size();
        }

        @Override
        public boolean isEmpty() { return this.parent.isEmpty(); }

        @Override
        public boolean contains(Object o) {
            for (WeakReference<V> ref : this.parent.map.values()) {
                if (ref == null) {
                    continue;
                }
                V value = ref.get();
                if (value == o) {
                    return true;
                } else if (value == null) {
                    return false;
                } else {
                    return value.equals(o);
                }
            }
            return false;
        }

        @Override
        public Iterator<V> iterator() {
            return new WeakIterator<>(this.parent.map.values().iterator());
        }

        @Override
        public Object[] toArray() {
            Object[] out = new Object[this.parent.size()];
            Iterator<WeakReference<V>> values = this.parent.map.values().iterator();
            for (int i = 0; i < out.length; ++i) {
                out[i] = values.next().get();
            }
            return out;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            Class<T[]> arrayType = (Class<T[]>) a.getClass();
            Class<T> componentType = (Class<T>) arrayType.getComponentType();
            if (a.length < this.parent.size()) {
                a = (T[]) Array.newInstance(componentType, this.parent.size());
            }
            Iterator<WeakReference<V>> values = this.parent.map.values().iterator();
            for (int i = 0; i < this.size(); ++i) {
                a[i] = componentType.cast(values.next());
            }
            return a;
        }

        @Override
        public boolean add(V e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            for (K key : this.parent.map.keySet()) {
                V value = this.parent.get(key);
                if (value == null) {
                    this.parent.remove(key, null);
                    continue;
                }
                if (value.equals(o)) {
                    return this.parent.remove(key, value);
                }
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!this.contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends V> c) {
            if (c == null || c.isEmpty()) {
                return false;
            }
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean changed = false;
            for (K key : this.parent.map.keySet()) {
                V value = this.parent.get(key);
                if (value == null) {
                    this.parent.remove(key, null);
                    continue;
                }
                if (c.contains(value)) {
                    changed |= this.parent.remove(key, value);
                }
            }
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean changed = false;
            for (K key : this.parent.map.keySet()) {
                V value = this.parent.get(key);
                if (value == null) {
                    this.parent.remove(key, null);
                    continue;
                }
                if (!c.contains(value)) {
                    changed |= this.parent.remove(key, value);
                }
            }
            return changed;
        }

        @Override
        public void clear() {
            this.parent.clear();
        }

    }

    @Override
    public Collection<V> values() {
        if (this.valueCollection == null) {
            this.valueCollection = new WeakValueHashMapValueCollection<>(this);
        }
        return this.valueCollection;
    }

    protected static class WeakValueHashMapEntrySet<K, V> implements Set<Entry<K, V>> {
        private final WeakValueHashMap<K, V> parent;
        private Map<K, WeakValueHashMapEntry<K, V>> entryCache = null;

        public WeakValueHashMapEntrySet(WeakValueHashMap<K, V> weakValueHashMap) {
            this.parent = weakValueHashMap;
        }

        protected final WeakValueHashMapEntry<K, V> getCachedEntry(K key) {
            if (this.entryCache == null) {
                return null;
            }
            return this.entryCache.get(key);
        }

        protected final WeakValueHashMapEntry<K, V> getEntry(K key) {
            WeakValueHashMapEntry<K, V> entry = this.getCachedEntry(key);
            if (entry == null) {
                entry = new WeakValueHashMapEntry<>(this.parent, key, this.parent.map.get(key));
                this.setEntry(key, entry);
            }
            return entry;
        }

        protected final void ensureFullyGenerateCache() {
            if (this.entryCache.size() != this.parent.size()) {
                if (this.entryCache.size() < this.parent.size()) {
                    for (K key : this.parent.keySet()) {
                        this.getEntry(key);
                    }
                }
                if (this.entryCache.size() > this.parent.size()) {
                    this.parent.removeDeadValues();
                }
            }
        }

        protected WeakValueHashMapEntry<K, V> setEntry(K key, WeakValueHashMapEntry<K, V> value) {
            if (this.entryCache == null) {
                this.entryCache = new HashMap<>();
            }
            return this.entryCache.put(key, value);
        }

        protected static class WeakValueHashMapEntry<K, V> implements Entry<K, V> {
            private final WeakValueHashMap<K, V> parent;
            private final K key;
            private WeakReference<V> value;

            public WeakValueHashMapEntry(WeakValueHashMap<K, V> parent, K key, WeakReference<V> value) {
                this.parent = parent;
                this.key = key;
                this.value = value;
            }

            @Override
            public K getKey() {
                return key;
            }

            @Override
            public V getValue() { return value.get(); }

            @Override
            public V setValue(V value) {
                return parent.put(this.key, value);
            }

        }

        @Override
        public int size() {
            return this.parent.size();
        }

        @Override
        public boolean isEmpty() {
            return this.parent.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Entry<?, ?>)) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) o;
            return this.parent.get(entry.getKey()).equals(entry.getValue());
        }

        private static final class WeakValueHashMapEntrySetIterator<K, V> implements Iterator<Entry<K, V>> {
            private final WeakValueHashMap<K, V> map;
            private final WeakValueHashMapEntrySet<K, V> set;
            private final Iterator<K> keyIter;

            public WeakValueHashMapEntrySetIterator(WeakValueHashMapEntrySet<K, V> set) {
                this.map = set.parent;
                this.set = set;
                this.keyIter = this.map.map.keySet().iterator();
            }

            @Override
            public boolean hasNext() {
                return this.keyIter.hasNext();
            }

            @Override
            public Entry<K, V> next() {
                return this.set.getEntry(this.keyIter.next());
            }
        }

        @Override
        public Iterator<Entry<K, V>> iterator() {
            return new WeakValueHashMapEntrySetIterator<>(this);
        }

        @Override
        public Object[] toArray() {
            this.ensureFullyGenerateCache();
            return this.entryCache.values().toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            this.ensureFullyGenerateCache();
            return this.entryCache.values().toArray(a);
        }

        @Override
        public boolean add(Entry<K, V> e) {
            return this.parent.put(e.getKey(), e.getValue()) == null;
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Entry<?, ?>)) {
                return false;
            }
            Entry<?, ?> entry = (Entry<?, ?>) o;
            return this.parent.remove(entry.getKey(), entry.getValue());
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            for (Object o : c) {
                if (!this.contains(o)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K, V>> c) {
            boolean changed = !c.isEmpty();
            for (Entry<K, V> entry : c) {
                this.add(entry);
            }
            return changed;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            boolean changed = false;
            for (K key : this.parent.keySet()) {
                WeakValueHashMapEntry<K, V> entry = this.getEntry(key);
                if (entry != null && c.contains(entry)) {
                    changed |= this.parent.remove(key, entry.getValue());
                }
            }
            return changed;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean changed = false;
            for (Object o : c) {
                changed |= this.remove(o);
            }
            return changed;
        }

        @Override
        public void clear() {
            this.parent.clear();
            if (this.entryCache != null) {
                this.entryCache.clear();
            }
        }

    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        if (this.entrySet == null) {
            this.entrySet = new WeakValueHashMapEntrySet<>(this);
        }
        return this.entrySet;
    }
}
