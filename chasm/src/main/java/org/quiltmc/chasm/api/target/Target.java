package org.quiltmc.chasm.api.target;

import org.quiltmc.chasm.api.tree.Node;

/**
 * Defines a range of {@link Node}s.
 */
public interface Target {
    /**
     * Gets a {@link Node} containing the {@link Target}ed {@code Node}[s].
     *
     * @return The targeted {@code Node}[s] as a {@code Node}.
     */
    Node getTarget();
}
