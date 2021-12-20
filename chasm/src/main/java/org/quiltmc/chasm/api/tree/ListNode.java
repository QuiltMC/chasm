package org.quiltmc.chasm.api.tree;

import java.util.List;

/**
 * Accesses child {@link Node}s by index.
 *
 * <p>Stores a {@code List} of child {@code Node}s.
 */
public interface ListNode extends Node, List<Node> {
    @Override
    ListNode deepCopy();

    @Override
    <P extends Node, W extends CowWrapperNode<P, W>> CowWrapperListNode asWrapper(CowWrapperNode<P, W> parent,
            Object key, boolean owned);
}
