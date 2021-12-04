package org.quiltmc.chasm.api.tree;

import java.util.List;

import org.quiltmc.chasm.internal.metadata.PathMetadata;

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

    @Override
    default ListNode<N> updatePath(PathMetadata path) {
        getMetadata().put(path);

        // Recursively set the path for all entries
        for (int i = 0; i < size(); i++) {
            get(i).updatePath(path.append(i));
        }
        return this;
    }
}
