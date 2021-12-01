package org.quiltmc.chasm.api.tree;

import java.util.LinkedHashMap;
import java.util.Map;

import org.quiltmc.chasm.internal.metadata.MetadataProvider;

/**
 * Use a {@link LinkedHashMap} to implement a {@link MapNode}.
 */
public class LinkedHashMapNode extends LinkedHashMap<String, Node> implements MapNode {
    private MetadataProvider metadataProvider = new MetadataProvider();

    @Override
    public LinkedHashMapNode copy() {
        LinkedHashMapNode copy = new LinkedHashMapNode();
        copy.metadataProvider = this.metadataProvider.copy();

        for (Map.Entry<String, Node> entry : this.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }

        return copy;
    }

    @Override
    public MetadataProvider getMetadata() {
        return this.metadataProvider;
    }
}
