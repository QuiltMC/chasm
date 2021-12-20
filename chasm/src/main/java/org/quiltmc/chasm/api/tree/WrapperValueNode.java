package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.metadata.MapMetadataProvider;

/**
 * Wraps a value as a {@link Node} for use in a CHASM tree.
 *
 * <p>Usually wraps an {@code Integer} or {@code String}.
 */
public class WrapperValueNode implements ValueNode {
    private final Object value;
    private MetadataProvider metadataProvider = new MapMetadataProvider();

    /**
     * Wraps the given {@code Object} value in a new {@link WrapperValueNode}.
     *
     * @param value The {@code Object} value to wrap.
     */
    public WrapperValueNode(Object value) {
        this.value = value;
    }

    /**
     * Gets the wrapped {@code Object} value.
     *
     * @return The wrapped {@code Object} value.
     */
    @Override
    public Object getValue() {
        return value;
    }

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
    @Override
    public <T> T getValueAs(Class<T> type) {
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new IllegalStateException("Value is not of expected type " + type
                    + ", but is " + value.getClass() + "!");
        }
        return type.cast(value);
    }

    /**
     * Gets the wrapped {@code String} value.
     *
     * @return The wrapped {@code String} value.
     *
     * @throws IllegalStateException If the value is not a {@code String}.
     */
    @Override
    public String getValueAsString() {
        return getValueAs(String.class);
    }

    /**
     * Gets the wrapped {@code Integer} value as an {@code int}.
     *
     * @return The doubly unwrapped {@code int} value.
     *
     * @throws IllegalStateException If the value is not an {@code Integer}.
     */
    @Override
    public int getValueAsInt() {
        Integer boxed = getValueAs(Integer.class);
        if (boxed == null) {
            throw new IllegalStateException("Value is null, but primitives can't be null!");
        }
        return boxed;
    }

    /**
     * Gets the wrapped {@code Boolean} value as an {@code boolean}.
     *
     * @return The doubly unwrapped {@code boolean} value.
     *
     * @throws IllegalStateException If the value is not a {@code Boolean}.
     */
    @Override
    public boolean getValueAsBoolean() {
        Boolean boxed = getValueAs(Boolean.class);
        if (boxed == null) {
            throw new IllegalStateException("Value is null, but primitives can't be null!");
        }
        return boxed;
    }

    @Override
    public ValueNode deepCopy() {
        WrapperValueNode copy = new WrapperValueNode(value);
        copy.metadataProvider = metadataProvider.deepCopy();
        return copy;
    }

    @Override
    public ValueNode shallowCopy() {
        WrapperValueNode copy = new WrapperValueNode(value);
        copy.metadataProvider = metadataProvider.shallowCopy();
        return copy;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }

    @Override
    public <P extends Node, W extends CowWrapperNode<P, W>> Node asWrapper(CowWrapperNode<P, W> parent, Object key,
            boolean owned) {
        // Only because of the metadata
        return new CowWrapperValueNode(parent, key, this, owned);
    }
}
