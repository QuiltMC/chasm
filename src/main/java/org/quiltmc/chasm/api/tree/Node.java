package org.quiltmc.chasm.api.tree;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.chasm.internal.metadata.MetadataProvider;

/**
 * Mark a compliant {@link Node} of a CHASM tree with this interface.
 */
public interface Node {
    /**
     * Create a deep copy of this {@link Node}.
     *
     * <p>This means that any containing node should also be copied,
     *           as well as the metadata of this Node.
     *
     * @return A recursive copy of this {@link Node}.
     */
    Node copy();

    /**
     * Return the {@link MetadataProvider} of this node.
     *
     * @return The {@link MetadataProvider} of this node.
     */
    @ApiStatus.Internal
    MetadataProvider getMetadata();
}
