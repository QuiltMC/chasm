package org.quiltmc.chasm.internal.tree;

import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LazyMap<K, V> implements Map<K, V> {
    private final Map<K, V> staticEntries;
    private final Supplier<Map<K, V>> lazyEntriesSupplier;
    private SoftReference<Map<K, V>> lazyEntries = new SoftReference<>(null);

    public LazyMap(Map<K, V> staticEntries, Supplier<Map<K, V>> lazyEntriesSupplier) {
        this.staticEntries = staticEntries;
        this.lazyEntriesSupplier = lazyEntriesSupplier;
    }

    public Map<K, V> getStaticEntries() {
        return staticEntries;
    }

    public Map<K, V> getLazyEntries() {
        Map<K, V> entries = lazyEntries.get();

        if (entries == null) {
            entries = lazyEntriesSupplier.get();
            lazyEntries = new SoftReference<>(entries);
        }

        return entries;
    }

    @Override
    public int size() {
        return getLazyEntries().size();
    }

    @Override
    public boolean isEmpty() {
        return staticEntries.isEmpty() && getLazyEntries().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return staticEntries.containsKey(key) || getLazyEntries().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return staticEntries.containsValue(value) || getLazyEntries().containsValue(value);
    }

    @Override
    public V get(Object key) {
        V val = staticEntries.get(key);

        if (val == null) {
            val = getLazyEntries().get(key);
        }

        return val;
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException("LazyMap is immutable.");
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("LazyMap is immutable.");
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException("LazyMap is immutable.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("LazyMap is immutable.");
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return getLazyEntries().keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return getLazyEntries().values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return getLazyEntries().entrySet();
    }
}
