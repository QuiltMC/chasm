package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.internal.metadata.Metadata;
import org.quiltmc.chasm.internal.metadata.MetadataProvider;

/**
 * Wrap a T value as a {@link Node} for use in a CHASM tree.
 *
 * <p>Usually wraps an {@link Integer} or {@link String}.
 *
 * @param <T> The type of value wrapped by this {@link ValueNode}.
 */
public class ValueNode<T> implements Node {
    private final T value;
    private MetadataProvider<? extends Metadata> metadataProvider = new MetadataProvider<>();

    /**
     * Wrap the given T value in a new {@link ValueNode}.
     *
     * @param value The T value to wrap.
     */
    public ValueNode(T value) {
        this.value = value;
    }

    protected ValueNode(T value, MetadataProvider<? extends Metadata> metadata) {
        this.value = value;
        this.metadataProvider = new MetadataProvider<>(metadata);
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
    public FrozenValueNode<T> asImmutable() {
        return new FrozenValueNode<>(this);
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }

    @Override
    public ValueNode<T> asMutable() {
        return this;
    }
}
