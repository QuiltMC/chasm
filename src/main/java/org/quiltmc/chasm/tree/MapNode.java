package org.quiltmc.chasm.tree;

import java.util.Map;

public interface MapNode extends Node, Map<String, Node> {
    @Override
    default MapNode toImmutable() {
        if (isImmutable()) {
            return this;
        }

        MapNode newNode = new LinkedHashMapNode();
        for (Entry<String, Node> entry : entrySet()) {
            newNode.put(entry.getKey(), entry.getValue().toImmutable());
        }

        return new ImmutableMapNode(newNode);
    }
}
