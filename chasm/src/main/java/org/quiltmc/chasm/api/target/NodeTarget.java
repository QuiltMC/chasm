package org.quiltmc.chasm.api.target;

import org.quiltmc.chasm.api.Lock;
import org.quiltmc.chasm.lang.api.ast.Node;

/**
 * Locates a {@link Node}, as a {@link Target}.
 */
public class NodeTarget extends Target {
    /**
     * Makes a new {@link NodeTarget} that targets the passed {@link Node}.
     *
     * @param node The {@link Node} to target.
     */
    public NodeTarget(Node node) {
        super(node, Lock.NONE);
    }

    /**
     * Makes a new {@link NodeTarget} that targets the passed {@link Node} and applies the specified {@link Lock}.
     *
     * @param node The {@link Node} to target.
     * @param lock The {@link Lock} to apply.
     */
    public NodeTarget(Node node, Lock lock) {
        super(node, lock);
    }
}
