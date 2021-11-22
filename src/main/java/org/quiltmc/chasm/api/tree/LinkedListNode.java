package org.quiltmc.chasm.api.tree;

import java.util.LinkedList;

import org.quiltmc.chasm.internal.metadata.MetadataProvider;

public class LinkedListNode extends LinkedList<Node> implements ListNode {
    private MetadataProvider metadataProvider = new MetadataProvider();

    @Override
    public LinkedListNode copy() {
        LinkedListNode copy = new LinkedListNode();
        copy.metadataProvider = metadataProvider.copy();

        for (Node entry : this) {
            copy.add(entry.copy());
        }

        return copy;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }
}
