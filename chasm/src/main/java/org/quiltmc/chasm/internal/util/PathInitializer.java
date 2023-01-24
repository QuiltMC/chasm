package org.quiltmc.chasm.internal.util;

import java.util.Map;

import org.quiltmc.chasm.internal.metadata.PathMetadata;
import org.quiltmc.chasm.internal.tree.ClassNode;
import org.quiltmc.chasm.lang.api.ast.ListNode;
import org.quiltmc.chasm.lang.api.ast.MapNode;
import org.quiltmc.chasm.lang.api.ast.Node;

public abstract class PathInitializer {
    private PathInitializer() {
    }

    public static void initialize(Node root, PathMetadata path) {
        // Set the path for the root
        root.getMetadata().put(PathMetadata.class, path);

        if (root instanceof ClassNode) {
            ClassNode lazyClassNode = (ClassNode) root;

            // Recursively set the path for all non-lazy entries
            for (Map.Entry<String, Node> entry : lazyClassNode.getStaticEntries().entrySet()) {
                initialize(entry.getValue(), new PathMetadata(path, entry.getKey()));
            }
        } else if (root instanceof MapNode) {
            MapNode mapNode = (MapNode) root;

            // Recursively set the path for all entries
            for (Map.Entry<String, Node> entry : mapNode.getEntries().entrySet()) {
                initialize(entry.getValue(), new PathMetadata(path, entry.getKey()));
            }
        } else if (root instanceof ListNode) {
            ListNode listNode = (ListNode) root;

            // Recursively set the path for all entries
            for (int i = 0; i < listNode.size(); i++) {
                initialize(listNode.get(i), new PathMetadata(path, i));
            }
        }
    }
}
