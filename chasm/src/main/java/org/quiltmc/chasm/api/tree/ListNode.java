package org.quiltmc.chasm.api.tree;

import java.util.List;

import org.quiltmc.chasm.internal.metadata.PathMetadata;

/**
 * Accesses child {@link Node}s by index.
 *
 * <p>Stores a {@code List} of child {@code Node}s.
 */
public interface ListNode extends Node, List<Node> {
    @Override
    FrozenListNode asImmutable();

    @Override
    default ListNode asMutable() {
        return this;
    }

    @Override
    default ListNode updatePath(PathMetadata path) {
        getMetadata().put(PathMetadata.class, path);

        // Recursively set the path for all entries
        for (int i = 0; i < size(); i++) {
            get(i).updatePath(path.append(i));
        }
        return this;
    }
}
