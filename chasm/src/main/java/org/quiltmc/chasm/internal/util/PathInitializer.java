package org.quiltmc.chasm.internal.util;

import java.util.Map;

import org.quiltmc.chasm.api.tree.ListNode;
import org.quiltmc.chasm.api.tree.MapNode;
import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.LazyClassNode;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

public abstract class PathInitializer {
    private PathInitializer() {
    }

    public static void initialize(Node root, PathMetadata path) {
        // Set the path for the root
        root.getMetadata().put(PathMetadata.class, path);

        if (root instanceof LazyClassNode) {
            LazyClassNode lazyClassNode = (LazyClassNode) root;

            // Recursively set the path for all non-lazy entries
            for (Map.Entry<String, Node> entry : lazyClassNode.getNonLazyEntrySet()) {
                initialize(entry.getValue(), path.append(entry.getKey()));
            }

            // Set the path for all lazy entries if they are loaded
            MapNode fullNode = lazyClassNode.getFullNodeOrNull();
            if (fullNode != null) {
                initialize(fullNode, path);
            }
        } else if (root instanceof MapNode) {
            MapNode mapNode = (MapNode) root;

            // Recursively set the path for all entries
            for (Map.Entry<String, Node> entry : mapNode.entrySet()) {
                initialize(entry.getValue(), path.append(entry.getKey()));
            }
        } else if (root instanceof ListNode) {
            ListNode listNode = (ListNode) root;

            // Recursively set the path for all entries
            for (int i = 0; i < listNode.size(); i++) {
                initialize(listNode.get(i), path.append(i));
            }
        }
    }
}
