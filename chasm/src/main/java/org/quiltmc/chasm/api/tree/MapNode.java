package org.quiltmc.chasm.api.tree;

import java.util.Map;

public interface MapNode extends Node, Map<String, Node> {
    @Override
    MapNode copy();
}
