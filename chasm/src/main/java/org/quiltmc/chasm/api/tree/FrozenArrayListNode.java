/**
 *
 */
package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;
import org.quiltmc.chasm.internal.tree.frozencollection.ImmutableArrayList;

public class FrozenArrayListNode extends ImmutableArrayList<FrozenNode> implements FrozenListNode<FrozenNode> {
    private FrozenMetadataProvider metadata;

    public FrozenArrayListNode(ListNode<? extends Node> mutableListNode) {
        super(new FrozenNode[mutableListNode.size()]);
        for (int i = 0; i < super.size(); ++i) {
            Node node = mutableListNode.get(i);
            super.set(i, node.asImmutable());
        }
        metadata = mutableListNode.getMetadata().freeze();
    }

    private FrozenArrayListNode(FrozenNode[] children, FrozenMetadataProvider metadata) {
        super(children);
        this.metadata = metadata;
    }

    @Override
    public FrozenMetadataProvider getFrozenMetadata() {
        return metadata;
    }

    @Override
    public FrozenArrayListNode asImmutable() {
        return this;
    }

    @Override
    public ListNode<Node> asMutable() {
        return new ArrayListNode(this);
    }

    @Override
    protected FrozenArrayListNode newList(FrozenNode[] path) {
        return new FrozenArrayListNode(path, metadata);
    }
}
