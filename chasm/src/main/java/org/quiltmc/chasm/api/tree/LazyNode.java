package org.quiltmc.chasm.api.tree;

/**
 * A {@link Node} that might be lazily initialized.
 * Modifying a {@link LazyNode} will replace it with its non-lazy version in the tree.
 */
public interface LazyNode extends Node {
    /**
     * Returns a non-lazy {@link Node} with equivalent contents to this {@link LazyNode}.
     *
     * @return A non-lazy version of this {@link Node}.
     */
    Node getFullNode();
}
