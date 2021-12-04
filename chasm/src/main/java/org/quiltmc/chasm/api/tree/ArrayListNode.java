package org.quiltmc.chasm.api.tree;

import java.util.ArrayList;

import org.quiltmc.chasm.internal.metadata.Metadata;
import org.quiltmc.chasm.internal.metadata.MetadataProvider;

/**
 * Use a {@link ArrayList} to implement a {@link ListNode}.
 */
public class ArrayListNode extends ArrayList<Node> implements ListNode<Node> {
    private MetadataProvider<Metadata> metadataProvider;

    public ArrayListNode() {
        super();
        metadataProvider = new MetadataProvider<>();
    }

    public ArrayListNode(ListNode<? extends Node> listNode) {
        super(listNode.size());
        metadataProvider = listNode.getMetadata().thaw();
        for (Node node : listNode) {
            ArrayListNode.this.add(node.asMutable());
        }
    }

    @Override
    public FrozenArrayListNode asImmutable() {
        return new FrozenArrayListNode(this);
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }
}
