package org.quiltmc.chasm.tree;

import org.quiltmc.chasm.transformer.NodePath;

public interface Node {
    /**
     * Recursively initialize the path of this node with the given path. Calling this method on a node twice should
     * throw an exception.
     *
     * @param path The path that points to this node.
     */
    void initializePath(NodePath path);

    NodePath getPath();

    Node toImmutable();

    boolean isImmutable();

    default String getAsString() {
        throw new ClassCastException("Node " + getPath().toString() + " is not a value node.");
    }

    default int getAsInt()  {
        throw new ClassCastException("Node " + getPath().toString() + " is not a value node.");
    }

    default boolean getAsBoolean() {
        throw new ClassCastException("Node " + getPath().toString() + " is not a value node.");
    }

    default Object getAsObject() {
        return this;
    }

    default <T> ValueNode<T> getAsValueNode()  {
        throw new ClassCastException("Node " + getPath().toString() + " is not a value node.");
    }

    default MapNode getAsMapNode() {
        throw new ClassCastException("Node " + getPath().toString() + " is not a map node.");
    }

    default ListNode getAsListNode() {
        throw new ClassCastException("Node " + getPath().toString() + " is not a list node.");
    }
}
