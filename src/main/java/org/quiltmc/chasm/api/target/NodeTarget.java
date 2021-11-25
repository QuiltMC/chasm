package org.quiltmc.chasm.api.target;

import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

public class NodeTarget implements Target {
    private final Node target;

    public NodeTarget(Node target) {
        this.target = target;
    }

    public Node getTarget() {
        return target;
    }
}
