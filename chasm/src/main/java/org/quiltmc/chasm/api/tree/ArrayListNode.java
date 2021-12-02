package org.quiltmc.chasm.api.tree;

import java.util.ArrayList;

import org.quiltmc.chasm.internal.metadata.MetadataProvider;


/**
 * Use a {@link ArrayList} to implement a {@link ListNode}.
 */
public class ArrayListNode extends ArrayList<Node> implements ListNode {
    private MetadataProvider metadataProvider = new MetadataProvider();

    @Override
    public ArrayListNode asImmutable() {
        ArrayListNode copy = new ArrayListNode();
        copy.metadataProvider = metadataProvider.copy();

        for (Node entry : this) {
            copy.add(entry.asImmutable());
        }

        return copy;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }
}
