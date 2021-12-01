package org.quiltmc.chasm.api.target;

import org.quiltmc.chasm.api.tree.Node;
import org.quiltmc.chasm.internal.metadata.PathMetadata;

/**
 * Navigate to a subtree of a {@link Node}, as a {@link Target}.
 */
public class NodeTarget implements Target {
    private final Node target;

    /**
     * Make a new {@link NodeTarget} that targets the passed node,
     * using the {@link Node}'s attached {@link PathMetadata}.
     *
     * @param target The {@link Node} to target.
     */
    public NodeTarget(Node target) {
        this.target = target;
    }

    @Override
    public Node getTarget() {
        return target;
    }
}
