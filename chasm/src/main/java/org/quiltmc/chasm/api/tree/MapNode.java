package org.quiltmc.chasm.api.tree;

import java.util.Map;

import org.quiltmc.chasm.internal.metadata.PathMetadata;

/**
 * Name child {@link Node}s with {@link String} names in this CHASM tree {@link Node}.
 */
public interface MapNode<N extends Node> extends Node, Map<String, N> {
    @Override
    FrozenMapNode asImmutable();

    @Override
    @SuppressWarnings("unchecked")
    default MapNode<Node> asMutable() {
        return (MapNode<Node>) this;
    }

    @Override
    default MapNode<N> updatePath(PathMetadata path) {
        getMetadata().put(path);

        // Recursively set the path for all entries
        for (Map.Entry<String, N> entry : entrySet()) {
            entry.getValue().updatePath(path.append(entry.getKey()));
        }

        return this;
    }
}
