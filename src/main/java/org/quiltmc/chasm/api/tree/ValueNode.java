package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.internal.metadata.MetadataProvider;

public class ValueNode<T> implements Node {
    private MetadataProvider metadataProvider = new MetadataProvider();

    private final T value;

    public ValueNode(T value) {
        this.value = value;
    }

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
