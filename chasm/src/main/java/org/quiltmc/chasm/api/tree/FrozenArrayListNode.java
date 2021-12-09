/**
 *
 */
package org.quiltmc.chasm.api.tree;

import org.quiltmc.chasm.internal.metadata.FrozenMetadataProvider;
import org.quiltmc.chasm.internal.tree.frozencollection.ImmutableArrayList;

// Safe because only frozen nodes are put in it at construction time, and never changed.
public class FrozenArrayListNode extends ImmutableArrayList<Node> implements FrozenListNode {
    private FrozenMetadataProvider metadata;

    public FrozenArrayListNode(ListNode mutableListNode) {
        super(new FrozenNode[mutableListNode.size()]);
        for (int i = 0; i < super.size(); ++i) {
            Node node = mutableListNode.get(i);
            super.set(i, node.asImmutable());
        }
        metadata = mutableListNode.getMetadata().freeze();
    }

    private FrozenArrayListNode(Node[] children, FrozenMetadataProvider metadata) {
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
    public ListNode asMutable() {
        return new ArrayListNode(this);
    }

    @Override
    protected FrozenArrayListNode newList(Node[] path) {
        return new FrozenArrayListNode(path, metadata);
    }
}
