package org.quiltmc.chasm.api.target;

import org.quiltmc.chasm.api.tree.Node;

/**
 * A {@link Target} encapsulates a range of {@link Node}s in a CHASM tree.
 */
public interface Target {
    /**
     * True if this {@link Target} fully contains the other.
     *
     * @param other The other {@link Target} to check against.
     *
     * @return True if this {@link Target} fully contains the other {@link Target}.
     */
    boolean contains(Target other);

    /**
     * True if the {@link Target}s overlap, but neither fully contains the other {@link Target}.
     *
     * @param other The other {@link Target} to check against.
     *
     * @return True if this {@link Target} overlaps the other.
     */
    boolean overlaps(Target other);

    /**
     * This method navigates the CHASM tree starting at the given {@link Node}.
     *
     * @param root The root {@link Node} to navigate from.
     *
     * @return The {@link Node} this {@link Target} led to.
     */
    Node resolve(Node root);
}
