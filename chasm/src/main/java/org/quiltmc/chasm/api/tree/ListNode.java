package org.quiltmc.chasm.api.tree;

import java.util.List;

import org.quiltmc.chasm.internal.tree.AbstractCowWrapperNode;

/**
 * Accesses child {@link Node}s by index.
 *
 * <p>Stores a {@code List} of child {@code Node}s.
 */
public interface ListNode extends Node, List<Node> {
    @Override
    ListNode deepCopy();

    @Override
    <P extends Node, W extends AbstractCowWrapperNode<P, W>> CowWrapperListNode asWrapper(AbstractCowWrapperNode<P, W> parent,
            Object key, boolean owned);
}
