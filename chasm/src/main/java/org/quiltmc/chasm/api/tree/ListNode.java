package org.quiltmc.chasm.api.tree;

import java.util.List;

/**
 * Store a {@link List} of child {@link Node}s in a CHASM tree {@link Node}.
 */
public interface ListNode<N extends Node> extends Node, List<N> {
    @Override
    FrozenListNode<FrozenNode> asImmutable();

    @Override
    @SuppressWarnings("unchecked")
    default ListNode<Node> asMutable() {
        return (ListNode<Node>) this;
    }
}
