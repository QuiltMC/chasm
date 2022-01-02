package org.quiltmc.chasm.api.tree;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.api.metadata.CowWrapperMetadataProvider;
import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.internal.cow.Copyable;
import org.quiltmc.chasm.internal.tree.AbstractCowWrapperNode;
import org.quiltmc.chasm.internal.util.NodeUtils;

/**
 * Marks a compliant {@link Node} of a CHASM tree.
 */
public interface Node extends Copyable {
    /**
     * Creates a deep copy of this {@link Node}.
     *
     * <p>This means that any contained nodes should also be copied,
     * as well as the metadata of all Nodes copied by this method.
     *
     * @return A recursive copy of this {@code Node}.
     */
    @Override
    Node deepCopy();

    @Override
    Node shallowCopy();

    /**
     * Returns the {@link MetadataProvider} of this node.
     *
     * @return The {@code MetadataProvider} of this node.
     */
    MetadataProvider getMetadata();

    /**
     * Sets the {@link MetadataProvider} of this node.
     *
     * @param other The new {@code MetadataProvider}
     * @param wrapper The {@link CowWrapperMetadataProvider} that wraps the given metadata provider, to discourage
     *            unauthorized calls.
     *
     * @return The old {@code MetadataProvider}
     */
    @ApiStatus.Internal
    MetadataProvider setMetadata(MetadataProvider other, CowWrapperMetadataProvider wrapper);

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
     * Casts the passed {@link Node} as a {@link WrapperValueNode}.
     *
     * @param node The {@code Node} to cast to a {@code WrapperValueNode}.
     *
     * @return The passed {@code Node} as a {@code WrapperValueNode}.
     *
     * @throws IllegalStateException If the passed {@code Node} is not a {@code WrapperValueNode}.
     *
     * @see Node#asList(Node)
     * @see Node#asMap(Node)
     */
    static WrapperValueNode asValue(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof ValueNode) {
            return (WrapperValueNode) node;
        }
        throw NodeUtils.createWrongTypeException(node, "WrapperValueNode");
    }

    <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperNode asWrapper(
            AbstractCowWrapperNode<P, W> parent, Object key, boolean owned);

}
