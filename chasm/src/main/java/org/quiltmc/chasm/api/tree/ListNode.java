package org.quiltmc.chasm.api.tree;

import java.util.List;

/**
 * Store a {@link List} of child {@link Node}s in a CHASM tree {@link Node}.
 */
public interface ListNode extends Node, List<Node> {
    @Override
    ListNode copy();
}
