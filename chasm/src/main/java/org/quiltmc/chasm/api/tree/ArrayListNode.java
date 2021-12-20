package org.quiltmc.chasm.api.tree;

import java.util.ArrayList;

import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.api.metadata.MapMetadataProvider;


/**
 * Uses an {@code ArrayList} to implement a {@link ListNode}.
 */
public class ArrayListNode extends ArrayList<Node> implements ListNode {
    private MetadataProvider metadataProvider = new MapMetadataProvider();


    @Override
    public ArrayListNode deepCopy() {
        ArrayListNode copy = new ArrayListNode();
        copy.metadataProvider = metadataProvider.deepCopy();

        for (Node entry : this) {
            copy.add(entry.deepCopy());
        }

        return copy;
    }

    @Override
    public ArrayListNode shallowCopy() {
        ArrayListNode copy = new ArrayListNode();
        copy.metadataProvider = metadataProvider.shallowCopy();
        copy.addAll(this);
        return copy;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }


    @Override
    public <P extends Node, W extends CowWrapperNode<P, W>> CowWrapperListNode asWrapper(CowWrapperNode<P, W> parent,
            Object key, boolean owned) {
        return new CowWrapperListNode(parent, key, this, owned);
    }
}
