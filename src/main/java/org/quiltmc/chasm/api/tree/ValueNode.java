package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.internal.metadata.MetadataProvider;

/**
 * A {@link ValueNode} wraps a T value as a {@link Node} for use in a CHASM tree.
 * Usually wraps an {@link Integer} or {@link String}.
 *
 * @param <T> The type of value wrapped by this {@link Node}
 */
public class ValueNode<T> implements Node {
    private MetadataProvider metadataProvider = new MetadataProvider();

    private final T value;

    /**
     * This simple constructor takes the T value to wrap,
     * and stores it in a newly created {@link ValueNode}.
     *
     * @param value The T value to wrap.
     */
    public ValueNode(T value) {
        this.value = value;
    }

    /**
     * This method gets the wrapped value.
     *
     * @return The wrapped {@link Object} value.
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
