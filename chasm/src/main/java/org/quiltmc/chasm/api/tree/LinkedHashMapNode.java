package org.quiltmc.chasm.api.tree;

import java.util.LinkedHashMap;
import java.util.Map;

import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.internal.tree.CowWrapperMapNode;
import org.quiltmc.chasm.api.metadata.MapMetadataProvider;

/**
 * Uses a {@linkplain LinkedHashMap} to implement a {@link MapNode}.
 */
public class LinkedHashMapNode extends LinkedHashMap<String, Node> implements MapNode {
    private MetadataProvider metadataProvider = new MapMetadataProvider();

    @Override
    public LinkedHashMapNode deepCopy() {
        LinkedHashMapNode copy = new LinkedHashMapNode();
        copy.metadataProvider = metadataProvider.deepCopy();

        for (Map.Entry<String, Node> entry : entrySet()) {
            copy.put(entry.getKey(), entry.getValue().deepCopy());
        }

        return copy;
    }

    @Override
    public Node shallowCopy() {
        LinkedHashMapNode copy = new LinkedHashMapNode();
        copy.metadataProvider = metadataProvider.shallowCopy();

        for (Map.Entry<String, Node> entry : entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }

        return copy;
    }

    @Override
    public MetadataProvider getMetadata() { return metadataProvider;
    }

    @Override
    public <P extends org.quiltmc.chasm.api.tree.Node, W extends CowWrapperNode<P, W>> org.quiltmc.chasm.api.tree.Node asWrapper(
            CowWrapperNode<P, W> parent, Object key, boolean owned) {
        return new CowWrapperMapNode(parent, key, this, owned);
    }
}
