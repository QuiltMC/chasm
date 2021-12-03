package org.quiltmc.chasm.api.tree;

import java.util.LinkedHashMap;
import java.util.Map;

import org.quiltmc.chasm.internal.metadata.Metadata;
import org.quiltmc.chasm.internal.metadata.MetadataProvider;

/**
 * Use a {@link LinkedHashMap} to implement a {@link MapNode}.
 */
public class LinkedHashMapNode extends LinkedHashMap<String, Node> implements MapNode<Node> {
    private MetadataProvider<Metadata> metadataProvider = new MetadataProvider<>();

    public LinkedHashMapNode() {
        super();
    }

    public LinkedHashMapNode(FrozenLinkedHashMapNode f) {
        super(f.size());
        Iterable<Map.Entry<String, FrozenNode>> frozenEntries = f.entrySet();
        for (Map.Entry<String, FrozenNode> frozenEntry : frozenEntries) {
            String key = frozenEntry.getKey();
            FrozenNode value = frozenEntry.getValue();
            Node thawedValue = value.asMutable();
            LinkedHashMapNode.this.put(key, thawedValue);
        }
        metadataProvider = f.getMetadata();
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
