/**
 *
 */
package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.api.metadata.MetadataProvider;

/**
 *
 */
public interface ValueNode extends Node {

    /**
     * Gets the wrapped {@code Object} value.
     *
     * @return The wrapped {@code Object} value.
     */
    Object getValue();

    /**
     * Gets the wrapped {@code Object} value cast to the given {@link Class}.
     *
     * @param <T> The type to cast this {@link WrapperValueNode}'s wrapped value to.
     *
     * @param type The {@code Class} to cast the wrapped value to.
     *
     * @return The wrapped value cast to the given {@code Class}.
     *
     * @throws IllegalStateException If the value is not an instance of the given {@code Class} according to
     *             {@link Class#isInstance(Object)}.
     */
    <T> T getValueAs(Class<T> type);

    /**
     * Gets the wrapped {@code String} value.
     *
     * @return The wrapped {@code String} value.
     *
     * @throws IllegalStateException If the value is not a {@code String}.
     */
    String getValueAsString();

    /**
     * Gets the wrapped {@code Integer} value as an {@code int}.
     *
     * @return The doubly unwrapped {@code int} value.
     *
     * @throws IllegalStateException If the value is not an {@code Integer}.
     */
    int getValueAsInt();

    /**
     * Gets the wrapped {@code Boolean} value as an {@code boolean}.
     *
     * @return The doubly unwrapped {@code boolean} value.
     *
     * @throws IllegalStateException If the value is not a {@code Boolean}.
     */
    boolean getValueAsBoolean();

    @Override
    ValueNode deepCopy();

    @Override
    MetadataProvider getMetadata();

}
