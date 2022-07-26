package org.quiltmc.chasm.internal.metadata;

import java.util.IdentityHashMap;
import java.util.Map;

import org.quiltmc.chasm.api.metadata.MetadataProvider;
import org.quiltmc.chasm.lang.api.ast.Node;

public class MetadataCache {
    private final Map<Node, MetadataProvider> cache = new IdentityHashMap<>();

    public MetadataProvider get(Node node) {
        return cache.computeIfAbsent(node, n -> new MetadataProvider());
    }

    public void put(Node node, MetadataProvider metadataProvider) {
        cache.put(node, metadataProvider);
    }
}
