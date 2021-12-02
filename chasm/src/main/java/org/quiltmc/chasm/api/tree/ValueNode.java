package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.internal.metadata.MetadataProvider;

public class ValueNode implements Node {
    private final Object value;
    private MetadataProvider metadataProvider = new MetadataProvider();

    public ValueNode(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public <T> T getValueAs(Class<T> type) {
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new IllegalStateException("Value is not of expected type " + type + ", but is " + value.getClass() + "!");
        }
        return type.cast(value);
    }

    public String getValueAsString() {
        return getValueAs(String.class);
    }

    public int getValueAsInt() {
        Integer boxed = getValueAs(Integer.class);
        if (boxed == null) {
            throw new IllegalStateException("Value is null, but primitives can't be null!");
        }
        return boxed;
    }

    public boolean getValueAsBoolean() {
        Boolean boxed = getValueAs(Boolean.class);
        if (boxed == null) {
            throw new IllegalStateException("Value is null, but primitives can't be null!");
        }
        return boxed;
    }

    @Override
    public ValueNode copy() {
        ValueNode copy = new ValueNode(value);
        copy.metadataProvider = metadataProvider.copy();
        return copy;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }
}
