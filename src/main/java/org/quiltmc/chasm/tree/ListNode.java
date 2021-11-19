package org.quiltmc.chasm.tree;

import java.util.List;

public interface ListNode extends Node, List<Node> {
    @Override
    default ListNode toImmutable() {
        if (isImmutable()) {
            return this;
        }

        ListNode newNode = new LinkedListNode();
        for (Node entry : this) {
            newNode.add(entry.toImmutable());
        }

        return new ImmutableListNode(newNode);
    }
}
