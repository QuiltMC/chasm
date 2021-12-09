package org.quiltmc.chasm.api.tree;

import java.util.LinkedHashMap;
import java.util.Map;

import org.quiltmc.chasm.api.metadata.MetadataProvider;

/**
 * Uses a {@linkplain LinkedHashMap} to implement a {@link MapNode}.
 */
public class LinkedHashMapNode extends LinkedHashMap<String, Node> implements MapNode {
    private MetadataProvider metadataProvider = new MetadataProvider();

    public LinkedHashMapNode() {
        super();
    }

    public LinkedHashMapNode(FrozenLinkedHashMapNode f) {
        super(f.size());
        Iterable<Map.Entry<String, Node>> frozenEntries = f.entrySet();
        for (Map.Entry<String, Node> frozenEntry : frozenEntries) {
            String key = frozenEntry.getKey();
            Node thawedValue = frozenEntry.getValue().asMutable();
            LinkedHashMapNode.this.put(key, thawedValue);
        }
        metadataProvider = f.getMetadata().thaw();
    }

    @Override
    public FrozenLinkedHashMapNode asImmutable() {
        return new FrozenLinkedHashMapNode(this);
    }

    @Override
    public MetadataProvider getMetadata() {
        return metadataProvider;
    }
}
