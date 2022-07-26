package org.quiltmc.chasm.api.metadata;

import java.util.HashMap;
import java.util.Map;

import org.quiltmc.chasm.lang.api.ast.Node;

/**
 * Provides {@link Metadata} attached to a {@link Node}.
 */
public class MetadataProvider {
    private final Map<Class<? extends Metadata>, Metadata> metadata;

    /**
     * Create a new, empty {@link MetadataProvider}.
     */
    public MetadataProvider() {
        this.metadata = new HashMap<>();
    }

    /**
     * Attach {@link Metadata} of a given type.
     *
     * @param dataClass The class of the type to add.
     * @param data The instance of the specified type to attach.
     * @param <T> The type of the metadata.
     */
    public <T extends Metadata> void put(Class<T> dataClass, T data) {
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
}
