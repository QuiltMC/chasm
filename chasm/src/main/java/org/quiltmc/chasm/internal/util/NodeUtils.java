package org.quiltmc.chasm.internal.util;

import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.ListPathMetadata;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

public class NodeUtils {
    private NodeUtils() {
    }

    public static IllegalStateException createWrongTypeException(Node node, String expectedType) {
        PathMetadata pathMeta = node.getMetadata().get(ListPathMetadata.class);
        if (pathMeta == null) {
            return new IllegalStateException("Node is not a " + expectedType + "!");
        } else {
            return new IllegalStateException("Node " + pathMeta + " is not a " + expectedType + "!");
        }
    }
}
