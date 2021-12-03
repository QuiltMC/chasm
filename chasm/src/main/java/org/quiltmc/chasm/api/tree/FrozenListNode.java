package org.quiltmc.chasm.api.tree;

public interface FrozenListNode<F extends FrozenNode> extends FrozenNode, ListNode<F> {
    @Override
    @SuppressWarnings("unchecked")
    default FrozenListNode<FrozenNode> asImmutable() {
        return (FrozenListNode<FrozenNode>) this;
    }

    @Override
    ListNode<Node> asMutable();
}
