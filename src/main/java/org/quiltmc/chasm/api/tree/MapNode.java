package org.quiltmc.chasm.api.tree;

import java.util.Map;

/**
 * Name child {@link Node}s with {@link String} names in this CHASM tree {@link Node}.
 */
public interface MapNode extends Node, Map<String, Node> {
    @Override
    MapNode copy();
}
