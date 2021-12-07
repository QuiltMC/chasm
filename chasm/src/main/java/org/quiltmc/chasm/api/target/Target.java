package org.quiltmc.chasm.api.target;

import org.quiltmc.chasm.api.Lock;
import org.quiltmc.chasm.api.tree.Node;

/**
 * Defines a range of {@link Node}s.
 */
public abstract class Target {
    private final Node node;
    private final Lock lock;

    public Target(Node node, Lock lock) {
        this.node = node;
        this.lock = lock;
    }

    /**
     * Gets a {@link Node} containing the {@link Target}.
     *
     * @return The targeted {@link Node}.
     */
    public Node getTarget() {
        return node;
    }

    /**
     * Gets the {@link Lock} applied to the {@link Target}.
     *
     * @return The targeted {@link Node}[s] as a {@link Node}.
     */
    public Lock getLock() {
        return lock;
    }
}
