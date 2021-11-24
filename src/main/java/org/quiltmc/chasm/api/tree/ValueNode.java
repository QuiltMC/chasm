package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.internal.metadata.MetadataProvider;

/**
 * Wrap a T value as a {@link Node} for use in a CHASM tree.
 *
 * <p>Usually wraps an {@link Integer} or {@link String}.
 *
 * @param <T> The type of value wrapped by this {@link ValueNode}.
 */
public class ValueNode<T> implements Node {
    private MetadataProvider metadataProvider = new MetadataProvider();

    private final T value;

    /**
     * Wrap the given T value in a newly created {@link ValueNode}.
     *
     * @param value The T value to wrap.
     */
    public ValueNode(T value) {
        this.value = value;
    }

    /**
     * Get the wrapped T value.
     *
     * @return The wrapped T value.
     */
    public T getValue() {
        return value;
    }

    @Override
    public ValueNode<T> copy() {
        ValueNode<T> copy = new ValueNode<>(value);
        copy.metadataProvider = metadataProvider.copy();
        return copy;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }
}
