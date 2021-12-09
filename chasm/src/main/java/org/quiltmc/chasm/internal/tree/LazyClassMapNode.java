/**
 *
 */
package org.quiltmc.chasm.internal.tree;

import java.util.Map;
import java.util.Set;
import org.objectweb.asm.ClassReader;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

public interface LazyClassMapNode extends MapNode {

    MapNode pollFullNode();

    MapNode getFullNode();

    ClassReader getClassReader();

    Set<Map.Entry<String, Node>> getNonLazyEntrySet();

    @Override
    default LazyClassMapNode updatePath(PathMetadata path) {
        LazyClassMapNode lazyClassNode = this;

        // Recursively set the path for all non-lazy entries
        for (Map.Entry<String, Node> entry : lazyClassNode.getNonLazyEntrySet()) {
            entry.getValue().updatePath(path.append(entry.getKey()));
        }

        // Set the path for all lazy entries if they are loaded
        MapNode fullNode = lazyClassNode.pollFullNode();
        if (fullNode != null) {
            fullNode.updatePath(path);
        }
        return this;
    }

}
