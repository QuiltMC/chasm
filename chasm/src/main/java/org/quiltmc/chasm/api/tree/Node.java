package org.quiltmc.chasm.api.tree;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.internal.metadata.MetadataProvider;
import org.quiltmc.chasm.internal.util.NodeUtils;

public interface Node {
    /**
     * Creates a deep copy of this {@link Node}.
     * This means that any containing node should also be copied.
     * This also copies the metadata of this Node.
     *
     * @return A recursive copy of this {@link Node}.
     */
    Node copy();

    /**
     * Return the {@link MetadataProvider} of this node.
     *
     * @return The {@link MetadataProvider} of this node.
     */
    @ApiStatus.Internal
    MetadataProvider getMetadata();

    static MapNode asMap(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof MapNode) {
            return (MapNode) node;
        }
        throw NodeUtils.createWrongTypeException(node, "MapNode");
    }

    static ListNode asList(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof ListNode) {
            return (ListNode) node;
        }
        throw NodeUtils.createWrongTypeException(node, "ListNode");
    }

    static ValueNode asValue(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof ValueNode) {
            return (ValueNode) node;
        }
        throw NodeUtils.createWrongTypeException(node, "ValueNode");
    }
}
