package org.quiltmc.chasm.api.target;

import org.quiltmc.chasm.api.tree.Node;

/**
 * Locates a {@link Node}, as a {@link Target}.
 */
public class NodeTarget implements Target {
    private final Node target;

    /**
     * Makes a new {@link NodeTarget} that targets the passed node.
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
