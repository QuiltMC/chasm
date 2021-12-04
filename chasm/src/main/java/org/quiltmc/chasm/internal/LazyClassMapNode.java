/**
 *
 */
package org.quiltmc.chasm.internal;

import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

public interface LazyClassMapNode<N extends Node> extends MapNode<N> {

    MapNode<N> pollFullNode();

    MapNode<N> getFullNode();

    ClassReader getClassReader();

    Set<Map.Entry<String, N>> getNonLazyEntrySet();

    @Override
    default LazyClassMapNode<N> updatePath(PathMetadata path) {
        @SuppressWarnings("unchecked")
        LazyClassMapNode<Node> lazyClassNode = (LazyClassMapNode<Node>) this;

        // Recursively set the path for all non-lazy entries
        for (Map.Entry<String, Node> entry : lazyClassNode.getNonLazyEntrySet()) {
            entry.getValue().updatePath(path.append(entry.getKey()));
        }

        // Set the path for all lazy entries if they are loaded
        MapNode<Node> fullNode = lazyClassNode.pollFullNode();
        if (fullNode != null) {
            fullNode.updatePath(path);
        }
        return this;
    }

}
