package org.quiltmc.chasm.api.tree;

import java.util.Map;

import org.quiltmc.chasm.internal.metadata.PathMetadata;

/**
 * Accesses child {@link Node}s by name.
 *
 * <p>Stores a {@code Map} of name strings to child {@code Node}s.
 */
public interface MapNode extends Node, Map<String, Node> {
    @Override
    FrozenMapNode asImmutable();

    @Override
    default MapNode asMutable() {
        return this;
    }

    @Override
    default MapNode updatePath(PathMetadata path) {
        getMetadata().put(PathMetadata.class, path);

        // Recursively set the path for all entries
        for (Entry<String, Node> entry : entrySet()) {
            entry.getValue().updatePath(path.append(entry.getKey()));
        }

        return this;
    }
}
