package org.quiltmc.chasm.tree;

import org.quiltmc.chasm.transformer.NodePath;

public class ValueNode<T> implements Node {
    private final T value;
    private final boolean immutable;
    private NodePath path = null;

    public ValueNode(T value) {
        this(value, null, false);
    }

    private ValueNode(T value, NodePath path, boolean immutable) {
        this.value = value;
        this.path = path;
        this.immutable = immutable;
    }

    public T getValue() {
        return value;
    }

    @Override
    public void initializePath(NodePath path) {
        if (this.path != null) {
            throw new RuntimeException("Path already initialized.");
        }

        this.path = path;
    }

    @Override
    public NodePath getPath() {
        return path;
    }

    @Override
    public Node toImmutable() {
        if (isImmutable()) {
            return this;
        }

        return new ValueNode<>(value, path, true);
    }

    @Override
    public boolean isImmutable() {
        return immutable;
    }
}
