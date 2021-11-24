package org.quiltmc.chasm.api.tree;

import java.util.Map;

/**
 * A CHASM tree {@link Node} with {@link String}-named child {@link Node}s.
 */
public interface MapNode extends Node, Map<String, Node> {
    @Override
    MapNode copy();
}
