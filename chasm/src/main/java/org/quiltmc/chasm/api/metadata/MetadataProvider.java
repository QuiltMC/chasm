/**
 *
 */
package org.quiltmc.chasm.api.metadata;

import org.quiltmc.chasm.internal.util.Copyable;

/**
 *
 */
public interface MetadataProvider extends Copyable {

    /**
     * Attach {@link Metadata} of a given type.
     *
     * @param dataClass The class of the type to add.
     * @param data The instance of the specified type to attach.
     * @param <T> The type of the metadata.
     */
    <T extends Metadata> void put(Class<T> dataClass, T data);

    /**
     * Retrieves {@link Metadata} of a given type.
     *
     * @param dataClass The class of the type to retrieve.
     * @param <T> The type of the metadata.
     * @return The attached metadata of the specified type, or {@code null} if it doesn't exist.
     */
    <T extends Metadata> T get(Class<T> dataClass);

    /**
     * Creates a deep copy of this {@link MetadataProvider}.
     *
     * <p>This means that all the contained {@link Metadata} will also be copied.
     *
     * @return A deep copy of this instance.
     */
    @Override
    MetadataProvider deepCopy();

    @Override
    MetadataProvider shallowCopy();
}
