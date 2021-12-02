package org.quiltmc.chasm.internal.metadata;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MetadataProvider<M extends Metadata> implements Iterable<M> {
    private final Map<Class<? extends M>, M> metadata;

    public MetadataProvider() {
        metadata = new HashMap<>();
    }

    protected MetadataProvider(Map<Class<? extends M>, M> map) {
        metadata = map;
    }

    @SuppressWarnings("unchecked")
    public <T extends M> void put(T data) {
        metadata.put((Class<? extends T>) data.getClass(), data);
    }

    @SuppressWarnings("unchecked")
    public <T extends M> T get(Class<T> dataClass) {
        return (T) metadata.get(dataClass);
    }

    @Override
    public Iterator<M> iterator() {
        return this.metadata.values().iterator();
    }

    public int size() {
        return metadata.size();
    }

    public FrozenMetadataProvider<FrozenMetadata> freeze() {
        Map<Class<? extends FrozenMetadata>, FrozenMetadata> frozenMap = new HashMap<>(this.metadata.size());
        for (M data : this.metadata.values()) {
            FrozenMetadata frozenData = data.freeze();
            frozenMap.put(frozenData.getClass(), frozenData);
        }

        return new FrozenMetadataProvider<>(frozenMap);
    }

    @SuppressWarnings("unchecked")
    public MetadataProvider<Metadata> thaw() {
        return (MetadataProvider<Metadata>) this;
    }
}
