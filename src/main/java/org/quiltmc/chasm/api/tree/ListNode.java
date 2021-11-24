package org.quiltmc.chasm.api.tree;

import java.util.List;

/**
 * A CHASM tree {@link Node} that stores a {@link List} of child {@link Node}s.
 */
public interface ListNode extends Node, List<Node> {
    @Override
    ListNode copy();
}
