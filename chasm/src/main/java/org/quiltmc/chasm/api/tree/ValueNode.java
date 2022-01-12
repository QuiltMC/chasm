package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.api.metadata.MetadataProvider;

/**
 * Wraps a value as a {@link Node} for use in a CHASM tree.
 *
 * <p>Usually wraps an {@code Integer} or {@code String}.
 */
public class ValueNode implements Node {
    private final Object value;
    private MetadataProvider metadataProvider = new MetadataProvider();

    /**
     * Wraps the given {@code Object} value in a new {@link ValueNode}.
     *
     * @param value The {@code Object} value to wrap.
     */
    public ValueNode(Object value) {
        this.value = value;
    }

    protected ValueNode(Object value, MetadataProvider metadata) {
        this.value = value;
        metadataProvider = metadata.copy();
    }

    /**
     * Gets the wrapped {@code Object} value.
     *
     * @return The wrapped {@code Object} value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Gets the wrapped {@code Object} value cast to the given {@link Class}.
     *
     * @param <T> The type to cast this {@link ValueNode}'s wrapped value to.
     *
     * @param type The {@code Class} to cast the wrapped value to.
     *
     * @return The wrapped value cast to the given {@code Class}.
     *
     * @throws IllegalStateException If the value is not an instance of the given {@code Class} according to
     *             {@link Class#isInstance(Object)}.
     */
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
    public boolean getValueAsBoolean() {
        Boolean boxed = getValueAs(Boolean.class);
        if (boxed == null) {
            throw new IllegalStateException("Value is null, but primitives can't be null!");
        }
        return boxed;
    }

    public ValueNode copy() {
        ValueNode copy = new ValueNode(value);
        copy.metadataProvider = metadataProvider.copy();
        return copy;
    }

    @Override
    public FrozenValueNode asImmutable() {
        return new FrozenValueNode(this);
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }

    @Override
    public ValueNode asMutable() {
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
