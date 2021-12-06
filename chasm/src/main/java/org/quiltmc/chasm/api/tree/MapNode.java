package org.quiltmc.chasm.api.tree;

import java.util.Map;

/**
 * Accesses child {@link Node}s by name.
 *
 * <p>Stores a {@code Map} of name strings to child {@code Node}s.
 */
public interface MapNode extends Node, Map<String, Node> {
    @Override
    MapNode copy();
}
