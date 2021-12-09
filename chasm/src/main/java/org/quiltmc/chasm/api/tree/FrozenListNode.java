package org.quiltmc.chasm.api.tree;

public interface FrozenListNode extends FrozenNode, ListNode {
    @Override
    default FrozenListNode asImmutable() {
        return this;
    }

    @Override
    ListNode asMutable();
}
