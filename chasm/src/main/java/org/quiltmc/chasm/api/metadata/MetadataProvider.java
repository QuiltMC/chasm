package org.quiltmc.chasm.api.metadata;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;

/**
 * Provides {@link Metadata} attached to a {@link org.quiltmc.chasm.api.tree.Node}.
 */
public class MetadataProvider implements Iterable<Class<? extends Metadata>> {
    private final Map<Class<? extends Metadata>, Metadata> metadata;

    /**
     * Create a new, empty {@link MetadataProvider}.
     */
    public MetadataProvider() {
        metadata = new HashMap<>();
    }

    public MetadataProvider(int size) {
        metadata = new HashMap<>(size);
    }

    public MetadataProvider(MetadataProvider other) {
        this(other.size());
        Iterator<Class<? extends Metadata>> metaIter = iterator();
        for (Class<? extends Metadata> clazz = metaIter.next(); metaIter.hasNext(); clazz = metaIter.next()) {
            Metadata data = MetadataProvider.this.get(clazz).thaw();
            MetadataProvider.this.putRaw(data.getClass(), data);
        }
    }

    /**
     * Attach {@link Metadata} of a given type.
     *
     * @param dataClass The class of the type to add.
     * @param data The instance of the specified type to attach.
     * @param <T> The type of the metadata.
     */
    @SuppressWarnings("unchecked")
    public <T extends Metadata> void put(Class<? super T> dataClass, T data) {
        metadata.put((Class<? extends Metadata>) dataClass, data);
    }

    protected void putRaw(Class<? extends Metadata> dataClass, Metadata data) {
        metadata.put(dataClass, data);
    }

    /**
     * Retrieves {@link Metadata} of a given type.
     *
     * @param dataClass The class of the type to retrieve.
     * @param <T> The type of the metadata.
     * @return The attached metadata of the specified type, or {@code null} if it doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public <T extends Metadata> T get(Class<T> dataClass) {
        return (T) metadata.get(dataClass);
    }

    /**
     * Creates a deep copy of this {@link MetadataProvider}.
     *
     * <p>This means that all the contained {@link Metadata} will also be copied.
     *
     * @return A deep copy of this instance.
     */
    public MetadataProvider copy() {
        MetadataProvider copy = new MetadataProvider(size());

        for (Map.Entry<Class<? extends Metadata>, Metadata> entry : metadata.entrySet()) {
            copy.metadata.put(entry.getKey(), entry.getValue().copy());
        }

        return copy;
    }

    @Override
    public Iterator<Class<? extends Metadata>> iterator() {
        return metadata.keySet().iterator();
    }

    public int size() {
        return metadata.size();
    }

    public FrozenMetadataProvider freeze() {
        return new FrozenMetadataProvider(this);
    }

    public MetadataProvider thaw() {
        return this;
    }
}
