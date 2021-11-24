package org.quiltmc.chasm.api.target;

import org.quiltmc.chasm.api.tree.Node;

/**
 * Navigate a CHASM tree to a {@link Target}ed range of {@link Node}s.
 */
public interface Target {
    /**
     * Check if this {@link Target} fully contains the other.
     *
     * @param other The other {@link Target} to check against.
     *
     * @return True if this {@link Target} fully contains the other {@link Target}.
     */
    boolean contains(Target other);

    /**
     * Check if the {@link Target}s only overlap.
     *
     * @param other The other {@link Target} to check against.
     *
     * @return True if this {@link Target} overlaps the other {@link Target}, but neither
     *          fully contains the other {@link Target}.
     */
    boolean overlaps(Target other);

    /**
     * Navigate the CHASM tree starting at the given {@link Node}.
     *
     * @param root The root {@link Node} to navigate from.
     *
     * @return A {@link Node} containing the {@link Target} range of {@link Node}s.
     */
    Node resolve(Node root);
}
