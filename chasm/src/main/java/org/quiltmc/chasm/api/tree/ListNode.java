package org.quiltmc.chasm.api.tree;

import java.util.List;

public interface ListNode extends Node, List<Node> {
    @Override
    ListNode copy();
}
