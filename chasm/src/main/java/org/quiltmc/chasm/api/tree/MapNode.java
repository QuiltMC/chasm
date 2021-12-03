package org.quiltmc.chasm.api.tree;

import java.util.Map;

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
}
