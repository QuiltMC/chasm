package org.quiltmc.chasm.api.tree;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.internal.metadata.MetadataProvider;
import org.quiltmc.chasm.internal.util.NodeUtils;

/**
 * Marks a compliant {@link Node} of a CHASM tree.
 */
public interface Node {
    /**
     * Creates a deep copy of this {@link Node}.
     *
     * <p>This means that any contained nodes should also be copied,
     * as well as the metadata of all Nodes copied by this method.
     *
     * @return A recursive copy of this {@code Node}.
     */
    Node copy();

    /**
     * Returns the {@link MetadataProvider} of this node.
     *
     * @return The {@code MetadataProvider} of this node.
     */
    @ApiStatus.Internal
    MetadataProvider getMetadata();

    /**
     * Casts the passed {@link Node} as a {@link MapNode}.
     *
     * @param node The {@code Node} to cast to a {@code MapNode}.
     *
     * @return The passed {@code Node} as a {@code MapNode}.
     *
     * @throws IllegalStateException If the passed {@code Node} is not a {@code MapNode}.
     *
     * @see Node#asList(Node)
     * @see Node#asValue(Node)
     */
    static MapNode asMap(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof MapNode) {
            return (MapNode) node;
        }
        throw NodeUtils.createWrongTypeException(node, "MapNode");
    }

    /**
     * Casts the passed {@link Node} as a {@link ListNode}.
     *
     * @param node The {@code Node} to cast to a {@code ListNode}.
     *
     * @return The passed {@code Node} as a {@code ListNode}.
     *
     * @throws IllegalStateException If the passed {@code Node} is not a {@code ListNode}.
     *
     * @see Node#asMap(Node)
     * @see Node#asValue(Node)
     */
    static ListNode asList(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof ListNode) {
            return (ListNode) node;
        }
        throw NodeUtils.createWrongTypeException(node, "ListNode");
    }

    /**
     * Casts the passed {@link Node} as a {@link ValueNode}.
     *
     * @param node The {@code Node} to cast to a {@code ValueNode}.
     *
     * @return The passed {@code Node} as a {@code ValueNode}.
     *
     * @throws IllegalStateException If the passed {@code Node} is not a {@code ValueNode}.
     *
     * @see Node#asList(Node)
     * @see Node#asMap(Node)
     */
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
