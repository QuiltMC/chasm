package org.quiltmc.chasm.lang.api.metadata;

import java.util.HashMap;
import java.util.Map;

import org.quiltmc.chasm.lang.api.ast.Node;

/**
 * Provides metadata attached to a {@link Node}.
 */
public class Metadata {
    private final Map<Class<?>, Object> metadata;

    /**
     * Create a new, empty {@link Metadata}.
     */
    public Metadata() {
        this.metadata = new HashMap<>();
    }

    /**
     * Attach metadata of a given type.
     *
     * @param dataClass The class of the type to add.
     * @param data The instance of the specified type to attach.
     * @param <T> The type of the metadata.
     * @return Previously attached metadata of the specified type, or {@code null} if it doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T put(Class<T> dataClass, T data) {
        return (T) metadata.put(dataClass, data);
    }

    /**
     * Retrieve metadata of a given type.
     *
     * @param dataClass The class of the type to retrieve.
     * @param <T> The type of the metadata.
     * @return The attached metadata of the specified type, or {@code null} if it doesn't exist.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> dataClass) {
        return (T) metadata.get(dataClass);
    }

    /**
     * Adds the given metadata to this metadata.
     * Existing metadata of a given type will be overwritten.
     *
     * @param metadata The metadata to add.
     */
    public void putAll(Metadata metadata) {
        this.metadata.putAll(metadata.metadata);
    }
}
