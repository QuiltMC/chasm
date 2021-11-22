package org.quiltmc.chasm.api.tree;

import java.util.HashMap;

import org.quiltmc.chasm.internal.metadata.MetadataProvider;

public class LinkedHashMapNode extends HashMap<String, Node> implements MapNode {
    private MetadataProvider metadataProvider = new MetadataProvider();

    @Override
    public LinkedHashMapNode copy() {
        LinkedHashMapNode copy = new LinkedHashMapNode();
        copy.metadataProvider = metadataProvider.copy();

        for (Entry<String, Node> entry : this.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }

        return copy;
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }
}
