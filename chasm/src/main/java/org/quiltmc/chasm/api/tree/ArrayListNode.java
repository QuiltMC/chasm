package org.quiltmc.chasm.api.tree;

import java.util.ArrayList;

import org.quiltmc.chasm.api.metadata.MetadataProvider;


/**
 * Uses an {@code ArrayList} to implement a {@link ListNode}.
 */
public class ArrayListNode extends ArrayList<Node> implements ListNode {
    private MetadataProvider metadataProvider;

    public ArrayListNode() {
        super();
        metadataProvider = new MetadataProvider();
    }

    public ArrayListNode(ListNode listNode) {
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
